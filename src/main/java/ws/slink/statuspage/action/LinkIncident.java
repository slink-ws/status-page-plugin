package ws.slink.statuspage.action;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/

public class LinkIncident extends AbstractIssueSelectAction {

//    private String     page = "";
//    private String incident = "";

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
//        ApplicationUser user = this.getLoggedInUser();

//        System.out.println("---     page: " + page);
//        System.out.println("--- incident: " + incident);


        // TODO: validate incident form here

        //        for (String username : watcherUserNames)
//        {
//            username = username.trim();
//
//            if (UserUtils.userExists(username))
//            {
//                validUsernames.add(username);
//            }
//            else
//            {
//                addErrorMessage(/*beanFactory.getInstance(getLoggedInUser()).getText("tutorial.errors.user")*/"error");
//            }
//        }
    }

    @Override
    public String doExecute() throws Exception {
//        for (String validUsername : validUsernames)
//        {
//            watcherManager.startWatching(UserUtils.getUser(validUsername), getIssueObject());
//        }
        // TODO: link incident here
        // ...
        // ...

        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
