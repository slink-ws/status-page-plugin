package ws.slink.statuspage.action;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.issue.AbstractIssueSelectAction;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.model.Component;
import ws.slink.statuspage.model.IssueIncident;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.JiraTools;
import ws.slink.statuspage.type.ComponentStatus;
import ws.slink.statuspage.type.IncidentSeverity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

// https://developer.atlassian.com/server/jira/platform/creating-an-ajax-dialog/

public class CreateIncident extends AbstractIssueSelectAction {

    private String page;
    private String impact;
    private String title;
    private String message;
    private String components;
    private String location;

    public void setPage(String value) {
        this.page = value;
    }
    public void setImpact(String value) {
        this.impact = value;
    }
    public void setTitle(String value) {
        this.title = value;
    }
    public void setMessage(String value) {
        this.message = value;
    }
    public void setComponents(String value) {
        this.components = value;
    }
    public void setLocation(String value) {
        this.location = value;
    }

    @Override
    public String doDefault() throws Exception {
        return INPUT;
    }

    @Override
    protected void doValidation() {
        ApplicationUser user = getLoggedInUser();
        if (null == user)
            addErrorMessage("user should be logged in to create incident");
        else if (!JiraTools.instance().isIncidentManager(getProject(), user))
            addErrorMessage("current user does not have incident management permissions");
//        System.out.println("page      : " + page);
//        System.out.println("impact    : " + impact);
//        System.out.println("title     : " + title);
//        System.out.println("message   : " + message);
//        System.out.println("components: " + components);
//        System.out.println("location  : " + location);

        // for error:
        // addErrorMessage(beanFactory.getInstance(getLoggedInUser()).getText("tutorial.errors.user", username));
    }

    @Override
    public String doExecute() throws Exception {
        AtomicReference<Map<String, String>> componentsMap = new AtomicReference<>(new HashMap<>());
        if (StringUtils.isNotBlank(components)) {
            JiraTools.instance().getGsonObject()
                .fromJson(components, Map.class)
                .entrySet()
                .stream()
                .forEach(e -> {
                    String key = (String) ((Map.Entry<?, ?>) e).getKey();
                    List<String> values = (List<String>) ((Map.Entry<?, ?>) e).getValue();
                    values.stream().forEach(v -> {
                        componentsMap.get().put(v, key);
                    });
                });
        }
        Issue issue = getIssueObject();
        StatuspageService.instance().get(issue.getProjectObject().getKey()).ifPresent(statusPage -> {
            List<Component> affectedComponents = Collections.emptyList();
            if (!componentsMap.get().isEmpty()) {
                affectedComponents = statusPage.components(page).stream().filter(c -> componentsMap.get().containsKey(c.id())).collect(Collectors.toList());
            }
            affectedComponents.stream().forEach(c -> c.status(ComponentStatus.of(componentsMap.get().get(c.id()))));
            statusPage.createIncident(page, title, message, IncidentSeverity.of(impact), null, affectedComponents).ifPresent(incident -> {

//                System.out.println("new incident created: " + incident.id());

                CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
                IssueIncident issueIncident = new IssueIncident()
                    .projectKey(issue.getProjectObject().getKey())
                    .incidentId(incident.id())
                    .pageId(page)
                    .createdBy(getLoggedInUser().getUsername())
                    .createdAt(LocalDateTime.now(ZoneId.of("UTC")))
                ;
                JiraTools.instance().setCustomFieldValue(issue, customField, issueIncident, true);
            });
        });

        if (StringUtils.isNotBlank(location))
            return returnCompleteWithInlineRedirect(location);
        else
            return returnCompleteWithInlineRedirect("/browse/" + getIssueObject().getKey());
    }

}
