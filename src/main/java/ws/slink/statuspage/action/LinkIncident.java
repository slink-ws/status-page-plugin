package ws.slink.statuspage.action;

import com.atlassian.jira.ComponentManagerStateImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import ws.slink.statuspage.customfield.IncidentCustomField;

import java.util.List;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/
// https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-programatically-create-CustomField/qaq-p/506266
public class LinkIncident extends /*JiraWebActionSupport/**/AbstractIssueSelectAction/**/ {

    private String     page;
    private String incident;

    public void setPage(String value) {
        this.page = value;
    }
    public void setIncident(String value) {
        this.incident = value;
    }

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
//        ApplicationUser user = this.getLoggedInUser();

        System.out.println("---      page: " + page);
        System.out.println("---  incident: " + incident);

        // TODO: validate incident form here
        addErrorMessage("error");

        Issue issue = getIssueObject();

//        CustomFieldManager customFieldManager = ComponentManager.getComponentInstanceOfType(CustomFieldManager.class);
//        List<CustomField> customFields = customFieldManager.getCustomFieldObjects();
//        customFields.stream().map(CustomField::getName).forEach(System.out::println);



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
