package ws.slink.statuspage.service;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;

import java.util.Optional;

// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class CustomFieldService {

    public static final String INCIDENT_CUSTOM_FIELD_KEY = "ws.slink.status-page-plugin:incident-custom-field";
    public static final String INCIDENT_CUSTOM_FIELD_DESCRIPTION = "Custom field for statuspage incident storage";

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

    public boolean isExists(String name) {
        return (null != get(name));
    }
    public boolean isCorrectType(String name, String typeKey) {
        CustomField field = get(name);
        if (null == field)
            return false;
        else if (!field.getCustomFieldType().getKey().equals(typeKey))
            return false;
        else
            return true;
    }
    public boolean create(String fieldId, String fieldKey, String fieldDescription) {

        FieldConfigScheme newConfigScheme = new FieldConfigScheme.Builder()
            .setName(fieldId)
            .setDescription(fieldDescription)
            .setFieldId(fieldKey)
            .toFieldConfigScheme()
        ;

        CustomFieldType type = ComponentAccessor.getComponent(CustomFieldManager.class).getCustomFieldType(fieldKey);

        System.out.println("type key : " + type.getKey());
        System.out.println("type name: " + type.getName());
        System.out.println("type desc: " + type.getDescription());

//        FieldConfigScheme scheme = ComponentAccessor.getComponent(FieldConfigSchemeManager.class)
//            .createFieldConfigScheme(newConfigScheme, contexts, issueTypes, field);

        return false;
    }


}
