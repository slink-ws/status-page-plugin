package ws.slink.condition;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.user.ApplicationUser;

public class IncidentExistsCondition {

    public boolean shouldDisplay(ApplicationUser applicationUser, Issue issue) {
        // TODO: check if incident exists for current issue and is allowed to be shown to current user
        return true;
    }

}
