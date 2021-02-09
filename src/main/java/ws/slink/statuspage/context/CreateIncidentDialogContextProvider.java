package ws.slink.statuspage.context;

import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

import java.util.HashMap;
import java.util.Map;

public class CreateIncidentDialogContextProvider extends AbstractJiraContextProvider {

    @Override
    public Map getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Map<String, Object> context = new HashMap<>();
        context.put("dialogWidth", 960);
        context.put("dialogHeight", 640);
        return context;
    }

}
