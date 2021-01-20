package ws.slink.statuspage.condition;

import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import ws.slink.statuspage.tools.JiraTools;

public class IncidentsConfiguredCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        return JiraTools.isIncidentsGlobalConfigReady()
            && JiraTools.isIncidentsProjectConfigReady(jiraHelper.getProject())
            && JiraTools.isIncidentsEnabled(jiraHelper.getProject())
        ;
    }

}
