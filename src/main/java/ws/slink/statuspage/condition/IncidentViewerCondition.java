package ws.slink.statuspage.condition;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import ws.slink.statuspage.tools.JiraTools;

public class IncidentViewerCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return JiraTools.isIncidentViewer(jiraHelper.getProject().getKey(), applicationUser)
            || JiraTools.isIncidentManager(jiraHelper.getProject().getKey(), applicationUser)
        ;
    }

}
