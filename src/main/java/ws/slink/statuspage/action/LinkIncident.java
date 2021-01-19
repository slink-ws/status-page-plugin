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
import ws.slink.statuspage.tools.JiraTools;

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
        Issue issue = getIssueObject();
        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        IssueIncident issueIncident = new IssueIncident()
            .projectKey(issue.getProjectObject().getKey())
            .incidentId(incident)
            .pageId(page)
            .linkedBy("...")
            .linkedAt(LocalDateTime.now(ZoneId.of("UTC")))
        ;
        JiraTools.setCustomFieldValue(issue, customField, issueIncident, true);
        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
