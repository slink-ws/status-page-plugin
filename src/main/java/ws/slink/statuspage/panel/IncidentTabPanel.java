package ws.slink.statuspage.panel;

import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import ws.slink.statuspage.tools.JiraTools;

import java.util.Collections;
import java.util.List;

public class IncidentTabPanel extends AbstractIssueTabPanel3 {

    @Override
    public boolean showPanel(ShowPanelRequest showPanelRequest) {
        return JiraTools.isIncidentsEnabled(showPanelRequest.issue().getProjectId())
            && JiraTools.isIncidentManager(showPanelRequest.issue().getProjectId(), showPanelRequest.remoteUser())
            && JiraTools.isIncidentExists(showPanelRequest.issue())
        ;
    }

    @Override
    public List<IssueAction> getActions(GetActionsRequest getActionsRequest) {
        return Collections.singletonList(new GenericMessageAction("incident tab details"));
    }
}
