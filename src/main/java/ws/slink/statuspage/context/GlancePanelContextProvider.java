package ws.slink.statuspage.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.customfield.IncidentCustomField;
import ws.slink.statuspage.model.Incident;
import ws.slink.statuspage.model.IssueIncident;
import ws.slink.statuspage.model.Page;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlancePanelContextProvider extends AbstractJiraContextProvider {
    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {

        Map<String, Object> contextMap = new HashMap<>();

        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");

        Object object = currentIssue.getCustomFieldValue(CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName()));
        if (null == object)
            return contextMap;

        IssueIncident issueIncident = null;
        if (object instanceof IssueIncident)
             issueIncident = (IssueIncident)object;
        else
            return contextMap;

        Optional<StatusPage> statusPage = StatuspageService.instance().get(currentIssue.getProjectObject().getKey());
        if (!statusPage.isPresent())
            return contextMap;

        Optional<Page> page = statusPage.get().getPage(issueIncident.pageId());
        if (!page.isPresent())
            return contextMap;

        Optional<Incident> incident = StatuspageService.instance().getIncident(currentIssue, true);//statusPage.get().getIncident(issueIncident.pageId(), issueIncident.incidentId(), true);
        if (!incident.isPresent())
            return contextMap;

//        System.out.println(incident.get());

        contextMap.put("page", page.get());
        contextMap.put("incident", incident.get());

        return contextMap;
    }
}
