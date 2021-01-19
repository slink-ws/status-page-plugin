package ws.slink.statuspage.action;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.tools.JiraTools;

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
        Issue issue = getIssueObject();
        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        //JiraTools.updateCustomFieldValue(issue, customField, null);
        JiraTools.setCustomFieldValue(issue, customField, null, true);
        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
