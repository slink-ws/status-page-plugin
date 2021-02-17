package ws.slink.statuspage.service;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericEntityException;
import ws.slink.statuspage.customfield.IncidentCustomFieldSearcher;

import java.util.*;
import java.util.stream.Collectors;

// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class CustomFieldService {

    public static final String INCIDENT_CUSTOM_FIELD_KEY = "ws.slink.status-page-plugin:incident-custom-field";
    public static final String INCIDENT_CUSTOM_FIELD_DESCRIPTION = "Custom field for statuspage incident storage";

    public static final String INCIDENT_CUSTOM_FIELD_SEARCHER_KEY = "ws.slink.status-page-plugin:incident-custom-field-searcher";

    private static class CustomFieldServiceSingleton {
        private static final CustomFieldService INSTANCE = new CustomFieldService();
    }
    public static CustomFieldService instance () {
        return CustomFieldService.CustomFieldServiceSingleton.INSTANCE;
    }

    public CustomField get(String name) {
        Optional<CustomField> existingIncidentCustomField =
                ComponentAccessor
                        .getComponent(CustomFieldManager.class)
                        .getCustomFieldObjectsByName(name)
                        .stream()
                        .filter(cf -> cf.getFieldName().equals(name))
                        .findAny();

        if (existingIncidentCustomField.isPresent())
            return existingIncidentCustomField.get();
        else
            return null;
    }
    public boolean exists(String name) {
        return get(name) != null;//ComponentAccessor.getComponent(CustomFieldManager.class).exists(name);
    }
    public boolean correct(String name, String typeKey) {
        CustomField field = get(name);
        if (null == field)
            return false;
        else if (!field.getCustomFieldType().getKey().equals(typeKey))
            return false;
        else
            return true;
    }
    public boolean update(String name, List<Project> projects) {
        CustomField field = get(name);
        if (null == field)
            return false;
//        System.out.println("--- update : ");
        ComponentAccessor.getComponent(FieldConfigSchemeManager.class).getConfigSchemesForField(field)
            .stream()
            .forEach(fcs -> {
//                System.out.println("id          : " + fcs.getId());
//                System.out.println("name        : " + fcs.getName());
//                System.out.println("desc        : " + fcs.getDescription());
//                System.out.println("contexts    : " + fcs.getContexts());
//                System.out.println("issue types : " + fcs.getAssociatedIssueTypes());
//                System.out.println("projects    : " + fcs.getAssociatedProjectObjects());
                ComponentAccessor.getComponent(FieldConfigSchemeManager.class)
                    .updateFieldConfigScheme(fcs, getContextsForProjects(projects), field);
            });
//        System.out.println("-----------------------------");
        return true;
    }
    public boolean create(String name, List<Project> projects) {

        // +
        // get custom field type for our custom field
        CustomFieldType type = ComponentAccessor.getComponent(CustomFieldManager.class).getCustomFieldType(INCIDENT_CUSTOM_FIELD_KEY);
        CustomFieldSearcher searcher = ComponentAccessor.getComponent(CustomFieldManager.class).getCustomFieldSearcher(INCIDENT_CUSTOM_FIELD_SEARCHER_KEY); // "com.atlassian.jira.plugin.system.customfieldtypes:exacttextsearcher"
        if (null == type)
            return false;

        // +
        // get JiraContextNodes for configured projects
        List<JiraContextNode> contexts = getContextsForProjects(projects);
        if (null == contexts || contexts.isEmpty())
            return false;

        // +
        // get IssueTypes for configured projects
        // Collection<IssueType> issueTypes = ComponentAccessor.getComponent(IssueTypeSchemeManager.class).getIssueTypesForDefaultScheme();
        Set<IssueType> issueTypes = projects
            .stream()
            .map(ComponentAccessor.getComponent(IssueTypeSchemeManager.class)::getIssueTypesForProject)
            .flatMap(it -> it.stream())
            .collect(Collectors.toSet())
        ;

        try {
            // create custom field for given contexts / issueTypes
            ComponentAccessor.getComponent(CustomFieldManager.class).createCustomField(
                name,
                INCIDENT_CUSTOM_FIELD_DESCRIPTION,
                type,
                searcher,
                contexts,
                new ArrayList<>(issueTypes)
            );
            return true;
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<JiraContextNode> getContextsForProjects(List<Project> projects) {
        Long [] projectIds = new Long[projects.size()];
        for (int i = 0; i < projectIds.length; i++)
            projectIds[i] = projects.get(i).getId();
        return CustomFieldUtils.buildJiraIssueContexts(false, projectIds, ComponentAccessor.getComponent(ProjectManager.class));
    }
}

//        System.out.println("type key : " + type.getKey());
//        System.out.println("type name: " + type.getName());
//        System.out.println("type desc: " + type.getDescription());
//        ComponentAccessor.getComponent(ProjectManager.class).getProjects()
//            .stream()
//            .map(ComponentAccessor.getComponent(IssueTypeSchemeManager.class)::getIssueTypesForProject)
//            .flatMap(it -> it.stream())
//            .forEach(issueTypes::add)
//        ;
//        FieldConfigScheme newConfigScheme = new FieldConfigScheme.Builder()
//            .setName(fieldId)
//            .setDescription(fieldDescription)
//            .setFieldId(fieldKey)
//            .toFieldConfigScheme()
//        ;
//        type.getCon
//        ComponentAccessor.getComponent(FieldConfigSchemeManager.class).getDefaultIssueTypeScheme()
//        FieldConfigScheme scheme = ComponentAccessor.getComponent(FieldConfigSchemeManager.class).get
//            .createFieldConfigScheme(newConfigScheme, contexts, issueTypes, field);
//        FieldConfigScheme newConfigScheme = new FieldConfigScheme.Builder()
//            .setName(fieldId)
//            .setDescription(fieldDescription)
//            .setFieldId(fieldKey)
//            .toFieldConfigScheme()
//        ;
//        public CustomField createCustomField(
//                String fieldName,
//                String description,
//                CustomFieldType fieldType,
//                CustomFieldSearcher customFieldSearcher,
//                List&lt;JiraContextNode&gt; contexts,
//                List&lt;IssueType&gt; issueTypes) throws GenericEntityException;


