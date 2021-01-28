package ws.slink.statuspage.action;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.tools.JiraTools;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/
// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class UnlinkIncident extends AbstractIssueSelectAction {

    private String location;

    public void setLocation(String value) {
        this.location = value;
    }

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
        JiraTools.instance().setCustomFieldValue(issue, customField, null, true);
        if (StringUtils.isNotBlank(location))
            return returnCompleteWithInlineRedirect(location);
        else
            return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
