package ws.slink.statuspage.action;

import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/

public class CreateIncident extends AbstractIssueSelectAction {

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
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
//                addErrorMessage(beanFactory.getInstance(getLoggedInUser()).getText("tutorial.errors.user", username));
//            }
//        }
        // TODO: validate incident form here
    }

    @Override
    public String doExecute() throws Exception {
//        for (String validUsername : validUsernames)
//        {
//            watcherManager.startWatching(UserUtils.getUser(validUsername), getIssueObject());
//        }
        // TODO: create incident here
        return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
