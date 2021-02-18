package ws.slink.statuspage.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import ws.slink.statuspage.model.Incident;
import ws.slink.statuspage.service.StatuspageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlancePanelContextProvider extends AbstractJiraContextProvider {
    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {

        Map<String, Object> contextMap = new HashMap<>();

        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");

        Optional<Incident> incident = StatuspageService.instance().getIncident(currentIssue);
        if (!incident.isPresent())
            return contextMap;

        contextMap.put("page", incident.get().page());
        contextMap.put("incident", incident.get());

        return contextMap;
    }
}
