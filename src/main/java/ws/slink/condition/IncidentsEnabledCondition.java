package ws.slink.condition;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class IncidentsEnabledCondition extends AbstractWebCondition {

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {

        System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
        System.out.println(jiraHelper.getContextParams());
        System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
        Issue issue = (Issue) jiraHelper.getContextParams().get("issue");
//        IssueService.IssueResult issueResult = ComponentAccessor.getIssueService().getIssue(applicationUser, );
        System.out.println(issue.toString());
        System.out.println(issue.getIssueType());
        System.out.println(issue.getAssignee());
        System.out.println(issue.getReporter());
        System.out.println(issue.getPriority());
        System.out.println(" ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");

//        System.err.println(" ~~~~~~~~~~~~~~~~~~~ ");

        return true;
    }

}
