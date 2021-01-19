package ws.slink.statuspage.action;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import ws.slink.statuspage.customfield.IncidentCustomField;
import ws.slink.statuspage.model.IssueIncident;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;

import java.time.LocalDateTime;
import java.time.ZoneId;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/
// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class UnlinkIncident extends AbstractIssueSelectAction {

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
    }

    @Override
    public String doExecute() throws Exception {
        // TODO: link incident here
        // ...
        // ...
        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        Issue issue = getIssueObject();
        Object cf = issue.getCustomFieldValue(customField);
        if (null != cf) {
            IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
            issueInputParameters.addCustomFieldValue(customField.getId(), null);

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
        }

        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
