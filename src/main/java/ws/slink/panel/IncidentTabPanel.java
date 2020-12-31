package ws.slink.panel;

import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import ws.slink.condition.IncidentExistsCondition;

import java.util.Collections;
import java.util.List;

public class IncidentTabPanel extends AbstractIssueTabPanel3 {


    @Override
    public boolean showPanel(ShowPanelRequest showPanelRequest) {
        return new IncidentExistsCondition().shouldDisplay(showPanelRequest.remoteUser(), showPanelRequest.issue());
    }

    @Override
    public List<IssueAction> getActions(GetActionsRequest getActionsRequest) {
        return Collections.singletonList(new GenericMessageAction("This is a message brought to you by the My Issue Tab Panel"));
    }
}
