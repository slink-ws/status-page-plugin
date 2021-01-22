package ws.slink.statuspage.panel;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel3;
import com.atlassian.jira.plugin.issuetabpanel.GetActionsRequest;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;
import com.atlassian.jira.plugin.issuetabpanel.ShowPanelRequest;
import com.atlassian.jira.util.VelocityParamFactory;
import com.atlassian.velocity.VelocityManager;
import ws.slink.statuspage.model.Component;
import ws.slink.statuspage.model.Incident;
import ws.slink.statuspage.model.IssueIncident;
import ws.slink.statuspage.model.Page;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.JiraTools;
import ws.slink.statuspage.type.IncidentStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class IncidentTabPanel extends AbstractIssueTabPanel3 {

    @Override
    public boolean showPanel(ShowPanelRequest showPanelRequest) {
        return JiraTools.isIncidentsEnabled(showPanelRequest.issue().getProjectId())
            && JiraTools.isIncidentManager(showPanelRequest.issue().getProjectId(), showPanelRequest.remoteUser())
            && JiraTools.isIncidentExists(showPanelRequest.issue())
        ;
    }

    @Override
    public List<IssueAction> getActions(GetActionsRequest getActionsRequest) {
        // https://stackoverflow.com/questions/30429303/how-do-i-render-a-velocity-template-inside-a-issue-tab-panel

        ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
        String baseUrl = ap.getString(APKeys.JIRA_BASEURL);
        String webworkEncoding = ap.getString(APKeys.JIRA_WEBWORK_ENCODING);
        VelocityManager vm = ComponentAccessor.getVelocityManager();
        VelocityParamFactory vp = ComponentAccessor.getVelocityParamFactory();

        Map<String, Object> context = vp.getDefaultVelocityParams();
        context.put("i18n", descriptor.getI18nBean());
        context.put("testVariable", "test variable value"); //["Value can be a string or an array or a collection"]);

        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        Object cfv = getActionsRequest.issue().getCustomFieldValue(customField);

        AtomicReference<List<Component>> loadedComponents = new AtomicReference<>(null);
        AtomicReference<Incident> loadedIncident          = new AtomicReference<>(null);
        if (null != cfv) {
            IssueIncident ii = (IssueIncident)cfv;
            context.put("issueIncident", ii);
            StatuspageService.instance().get(getActionsRequest.issue().getProjectObject().getKey()).ifPresent(statusPage -> {
                statusPage.getPage(ii.pageId()).ifPresent(page -> {
                    context.put("page", page);
                    loadedComponents.set(statusPage.components(page, true));
                });
                statusPage.getIncident(ii.pageId(), ii.incidentId(), true).ifPresent(incident -> {
                    context.put("incident", incident);
                    loadedIncident.set(incident);
                });
            });
        }

        if (null != loadedIncident.get() && null != loadedComponents.get()) {
            context.put("incidentStatuses", StatuspageService.instance().incidentStatusList(loadedIncident.get().isScheduled()));
            context.put("nonAffectedComponents", StatuspageService.instance().nonAffectedComponentsList(loadedComponents.get(), loadedIncident.get()));
        }
        context.put("componentStatuses", StatuspageService.instance().componentStatusList());
        context.put("incidentImpacts", StatuspageService.instance().incidentImpactList());
        context.put("issueKey", getActionsRequest.issue().getKey());

        String renderedText = vm.getEncodedBody("templates/panels/", "incident-tab-panel.vm", baseUrl, webworkEncoding, context);
        return Collections.singletonList(new GenericMessageAction(renderedText));
    }
}
