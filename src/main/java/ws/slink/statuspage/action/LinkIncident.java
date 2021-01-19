package ws.slink.statuspage.action;

import com.atlassian.jira.ComponentManagerStateImpl;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import ws.slink.statuspage.customfield.IncidentCustomField;
import ws.slink.statuspage.model.IssueIncident;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/
// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class LinkIncident extends /*JiraWebActionSupport/**/AbstractIssueSelectAction/**/ {

    private String     page;
    private String incident;

    public void setPage(String value) {
        this.page = value;
    }
    public void setIncident(String value) {
        this.incident = value;
    }

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
//        ApplicationUser user = this.getLoggedInUser();

        System.out.println("---      page: " + page);
        System.out.println("---  incident: " + incident);

        // TODO: validate incident form here
//        addErrorMessage("error");

        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        if (null == customField) {
            addErrorMessage("statuspage incident custom field not configured");
        } else {
        }

//        CustomFieldManager customFieldManager = ComponentManager.getComponentInstanceOfType(CustomFieldManager.class);
//        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
//        customFields.stream().map(CustomField::getName).forEach(System.out::println);



        //        for (String username : watcherUserNames)
//        {
//            username = username.trim();
//
//            if (UserUtils.userExists(username))
//            {
//                validUsernames.add(username);
//            }
//            else
//            {
//                addErrorMessage(/*beanFactory.getInstance(getLoggedInUser()).getText("tutorial.errors.user")*/"error");
//            }
//        }



    }

    @Override
    public String doExecute() throws Exception {
//        for (String validUsername : validUsernames)
//        {
//            watcherManager.startWatching(UserUtils.getUser(validUsername), getIssueObject());
//        }
        // TODO: link incident here
        // ...
        // ...


        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        Issue issue = getIssueObject();
        Object cf = issue.getCustomFieldValue(customField);
        if (null == cf) {
            IssueIncident issueIncident = new IssueIncident()
                .projectKey(issue.getProjectObject().getKey())
                .incidentId(incident)
                .pageId(page)
                .linkedBy("...")
                .linkedAt(LocalDateTime.now(ZoneId.of("UTC")))
            ;
            String value = issueIncident.toJsonString();

            IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
            issueInputParameters.addCustomFieldValue(customField.getId(), value);

            IssueService.UpdateValidationResult updateValidationResult =
                ComponentManager.getComponentInstanceOfType(IssueService.class)
                    .validateUpdate(getLoggedInUser(), issue.getId(), issueInputParameters);

            if (updateValidationResult.isValid()) {
                IssueService.IssueResult updateResult =
                    ComponentManager.getComponentInstanceOfType(IssueService.class)
                        .update(getLoggedInUser(), updateValidationResult);
                if (!updateResult.isValid()) {
                    System.out.println(" ---> unsuccessful update");
                } else {
                    System.out.println(" ---> successful update");
                }
            } else {
                System.out.println(" ---> updateValidationResult is invalid");
            }
        } else {
//            IncidentCustomField icf = (IncidentCustomField) cf;
            System.out.println(" ---> custom field value is not null");
        }


        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
