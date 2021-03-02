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
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.Common;
import ws.slink.statuspage.tools.JiraTools;
import ws.slink.statuspage.type.IncidentStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class IncidentTabPanel extends AbstractIssueTabPanel3 {

    @Override
    public boolean showPanel(ShowPanelRequest showPanelRequest) {
        return JiraTools.instance().isIncidentsEnabled(showPanelRequest.issue().getProjectId())
            && JiraTools.instance().isIncidentManager(showPanelRequest.issue().getProjectId(), showPanelRequest.remoteUser())
            && JiraTools.instance().isIncidentExists(showPanelRequest.issue())
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

        CustomField customField = CustomFieldService.instance().get(ConfigService.instance().getAdminCustomFieldName());
        Object cfv = getActionsRequest.issue().getCustomFieldValue(customField);

        AtomicReference<List<Component>> loadedComponents = new AtomicReference<>(null);
        AtomicReference<Incident> loadedIncident          = new AtomicReference<>(null);
        if (null != cfv) {
            IssueIncident ii = (IssueIncident)cfv;
            context.put("issueIncident", ii);
            StatuspageService.instance().getIncident(getActionsRequest.issue())
                .ifPresent(incident -> {
                    context.put("incident", incident);
                    context.put("incidentClosed", incident.status() == IncidentStatus.COMPLETED || incident.status() == IncidentStatus.RESOLVED);
                    loadedIncident.set(incident);
                    context.put("componentsOriginal", JiraTools.instance().getGsonObject().toJson(incident.components()));
                    context.put("defaultMessage", Common.getDefaultStatusMessage(incident.status()));
                    context.put("page", incident.page());
                    loadedComponents.set(StatuspageService.instance().getComponents(getActionsRequest.issue().getProjectObject().getKey(), incident.page().id()));
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
