package ws.slink.statuspage.servlet;

import bsh.StringUtil;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.error.ServiceCallException;
import ws.slink.statuspage.error.StatusPageException;
import ws.slink.statuspage.model.AffectedComponentStatus;
import ws.slink.statuspage.model.Component;
import ws.slink.statuspage.model.Group;
import ws.slink.statuspage.model.Incident;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.Common;
import ws.slink.statuspage.tools.JiraTools;
import ws.slink.statuspage.type.ComponentStatus;
import ws.slink.statuspage.type.IncidentSeverity;
import ws.slink.statuspage.type.IncidentStatus;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Scanned
@Path("/")
public class RestResource {

    @ComponentImport private final UserManager userManager;
    @ComponentImport private final TransactionTemplate transactionTemplate;

    @Inject
    public RestResource(UserManager userManager,
                        TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.transactionTemplate = transactionTemplate;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class AdminParams {
        @XmlElement private String projects;
        @XmlElement private String roles;
        @XmlElement private String custom_field;
        public String getProjects() {
            return projects;
        }
        public AdminParams setProjects(String projects) {
            this.projects = projects;
            return this;
        }
        public String getRoles() {
            return roles;
        }
        public AdminParams setRoles(String roles) {
            this.roles = roles;
            return this;
        }
        public String getCustomFieldId() {
            return custom_field;
        }
        public AdminParams setCustomFieldId(String value) {
            this.custom_field = value;
            return this;
        }
        public String toString() {
            return projects + " : " + roles + " : " + custom_field;
        }
        public AdminParams log(String prefix) {
            System.out.println(prefix + this);
            return this;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class ConfigParams {
        @XmlElement private String project;
        @XmlElement private String apikey;
        @XmlElement private String mgmt_roles;
        @XmlElement private String view_roles;
        public String getProject() {
            return project;
        }
        public ConfigParams setProject(String value) {
            this.project = value;
            return this;
        }
        public String getApikey() {
            return apikey;
        }
        public ConfigParams setApikey(String value) {
            this.apikey = value;
            return this;
        }
        public String getMgmtRoles() {
            return mgmt_roles;
        }
        public ConfigParams getMgmtRoles(String value) {
            this.mgmt_roles = value;
            return this;
        }
        public String getViewRoles() {
            return view_roles;
        }
        public ConfigParams getViewRoles(String value) {
            this.view_roles = value;
            return this;
        }
        public String toString() {
            return project + " : " + apikey + " : " + mgmt_roles + " : " + view_roles;
        }
        public ConfigParams log(String prefix) {
            System.out.println(prefix + this);
            return this;
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class IncidentUpdateParams {

        @XmlElement private String project;
        @XmlElement private String issue;
        @XmlElement private String page;
        @XmlElement private String incident;
        @XmlElement private String status;
        @XmlElement private String impact;
        @XmlElement private String message;
        @XmlElement private boolean publishComment;
        @XmlElement private Map<String, List<String>> components;

        public String getProject() {
            return project;
        }
        public IncidentUpdateParams setProject(String value) {
            this.project = value;
            return this;
        }
        public String getIssue() {
            return issue;
        }
        public IncidentUpdateParams setIssue(String value) {
            this.issue = value;
            return this;
        }
        public String getPage() {
            return page;
        }
        public IncidentUpdateParams setPage(String value) {
            this.page = value;
            return this;
        }
        public String getIncident() {
            return incident;
        }
        public IncidentUpdateParams setIncident(String value) {
            this.incident = value;
            return this;
        }
        public String getStatus() {
            return status;
        }
        public IncidentUpdateParams setStatus(String value) {
            this.status = value;
            return this;
        }
        public String getImpact() {
            return impact;
        }
        public IncidentUpdateParams setImpact(String value) {
            this.impact = value;
            return this;
        }
        public String getMessage() {
            return message;
        }
        public IncidentUpdateParams setMessage(String value) {
            this.message = value;
            return this;
        }
        public Map<String, List<String>> getComponents() {
            return components;
        }
        public IncidentUpdateParams setComponents(Map<String, List<String>> value) {
            this.components = value;
            return this;
        }

        public String toString() {
            return project + " " + issue + " " + page + " " + incident + " " + status + " " + impact + " " + message + " " + components;
        }
        public IncidentUpdateParams log(String prefix) {
            System.out.println(prefix + this);
            return this;
        }
    }

    @PUT
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAdminParams(final AdminParams config, @Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUser().getUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        AtomicBoolean saveResult = new AtomicBoolean(true);
        AtomicReference<String> message    = new AtomicReference<>("");

        transactionTemplate.execute(/*(TransactionCallback)*/ () -> {
//            config.log("~~~ received configuration: ");

            if (CustomFieldService.instance().exists(config.getCustomFieldId())) {
                if (CustomFieldService.instance().correct(config.getCustomFieldId(), CustomFieldService.INCIDENT_CUSTOM_FIELD_KEY)) {
                    System.out.println("custom field exists; update custom field & save");
                    if (!CustomFieldService.instance().update(
                        config.getCustomFieldId(),
                        getConfiguredProjects(config.getProjects())
                    )) {
                        saveResult.set(false);
                        message.set("could not create custom field '" + config.getCustomFieldId() + "'");
                    };

                } else {
                    saveResult.set(false);
                    message.set("incorrect type-key for custom field '" + config.getCustomFieldId() + "': " +
                        CustomFieldService.instance().get(config.getCustomFieldId()).getCustomFieldType().getKey());
                }
            } else {
                System.out.println("custom field does not exist; create custom field & save");
                if (!CustomFieldService.instance().create(
                    config.getCustomFieldId(),
                    CustomFieldService.INCIDENT_CUSTOM_FIELD_KEY,
                    CustomFieldService.INCIDENT_CUSTOM_FIELD_DESCRIPTION,
                    getConfiguredProjects(config.getProjects())
                )) {
                    saveResult.set(false);
                    message.set("could not create custom field '" + config.getCustomFieldId() + "'");
                };
            }

            if (saveResult.get()) {
                ConfigService.instance().setAdminProjects(config.getProjects());
                ConfigService.instance().setAdminRoles(config.getRoles());
                ConfigService.instance().setAdminCustomFieldName(config.getCustomFieldId());
                StatuspageService.instance().clear();
                ConfigService.instance().getAdminProjects().stream().forEach(p ->
                    StatuspageService.instance().init(p, ConfigService.instance().getConfigApiKey(p))
                );
            }

            return null;
        });

        if (saveResult.get()) {
            return Response.noContent().build();
        } else {
            return Response.status(400).entity(message.get()).build();
        }
    }

    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putConfigParams(final ConfigParams config, @Context HttpServletRequest request) {
        if (!JiraTools.instance().isPluginManager(userManager.getRemoteUser())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        transactionTemplate.execute((TransactionCallback) () -> {
//            config.log("~~~ received plugin config: ");
            ConfigService.instance().setConfigMgmtRoles(config.getProject(), config.getMgmtRoles());
            ConfigService.instance().setConfigViewRoles(config.getProject(), config.getViewRoles());
            ConfigService.instance().setConfigApiKey   (config.getProject(), config.getApikey());
            StatuspageService.instance().clear();
            ConfigService.instance().getAdminProjects().stream().forEach(p ->
                StatuspageService.instance().init(p, ConfigService.instance().getConfigApiKey(p))
            );
            return null;
        });
        return Response.noContent().build();
    }

    @GET
    @Path("/api/pages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pages(
        @QueryParam("issueKey") String issueKey,
        @Context HttpServletRequest request) {

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(JiraTools.instance().getGsonObject().toJson(statusPage.get().pages())).build();
    }

    @GET
    @Path("/api/components")
    @Produces(MediaType.APPLICATION_JSON)
    public Response components(
        @QueryParam("issueKey") String issueKey,
        @QueryParam("pageId") String pageId,
        @Context HttpServletRequest request) {

        if (StringUtils.isBlank(pageId))
            return Response.noContent().build();

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(JiraTools.instance().getGsonObject().toJson(statusPage.get().components(pageId))).build();
    }

    @GET
    @Path("/api/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response groups(
        @QueryParam("issueKey") String issueKey,
        @QueryParam("pageId") String pageId,
        @Context HttpServletRequest request) {

        if (StringUtils.isBlank(pageId))
            return Response.noContent().build();

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(
            JiraTools.instance().getGsonObject().toJson(
                statusPage
                    .get()
                    .groups(pageId)
                    .stream()
                    .sorted(Comparator.comparing(Group::name))
                    .collect(Collectors.toList())
            )
        ).build();
    }

    @GET
    @Path("/api/groupComponents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response groupComponents(
        @QueryParam("issueKey") String issueKey,
        @QueryParam("pageId") String pageId,
        @QueryParam("groupId") String groupId,
        @Context HttpServletRequest request) {

        if (StringUtils.isBlank(pageId))
            return Response.noContent().build();

        if (StringUtils.isBlank(groupId))
            return Response.noContent().build();

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(JiraTools.instance().getGsonObject().toJson(statusPage.get().groupComponents(pageId, groupId))).build();
    }

    @GET
    @Path("/api/nonGroupComponents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response nonGroupComponents(
        @QueryParam("issueKey") String issueKey,
        @QueryParam("pageId") String pageId,
        @Context HttpServletRequest request) {

        if (StringUtils.isBlank(pageId))
            return Response.noContent().build();

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(JiraTools.instance().getGsonObject().toJson(
            statusPage
                .get()
                .nonGroupComponents(pageId)
                .stream()
                .filter(c -> !c.group())
                .collect(Collectors.toList())
            )
        ).build();
    }

    @GET
    @Path("/api/incidents")
    @Produces(MediaType.APPLICATION_JSON)
    public Response incidents(
        @QueryParam("issueKey") String issueKey,
        @QueryParam("pageId") String pageId,
        @DefaultValue("false") @QueryParam("activeOnly") boolean activeOnly,
        @Context HttpServletRequest request) {

        if (StringUtils.isBlank(pageId))
            return Response.noContent().build();

        Optional<Project> project = JiraTools.instance().getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.instance().isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        if (activeOnly) {
            return Response.ok(JiraTools.instance().getGsonObject().toJson(
                statusPage.get().incidents(pageId)
                    .stream()
                    .filter(
                        i -> i.status() != IncidentStatus.RESOLVED
                          && i.status() != IncidentStatus.COMPLETED
                    ).collect(Collectors.toList())
            )).build();
        } else {
            return Response.ok(JiraTools.instance().getGsonObject().toJson(statusPage.get().incidents(pageId))).build();
        }
    }


    @PUT
    @Path("/api/incident")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateIncident(final IncidentUpdateParams incidentUpdateParams, @Context HttpServletRequest request) {
//        System.out.println("----> SAVING INCIDENT: " + incidentUpdateParams);

        String messageEscaped = null;

        AtomicInteger resultCode              = new AtomicInteger(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        AtomicBoolean resultStatus            = new AtomicBoolean(false);
        AtomicReference<String> resultMessage = new AtomicReference<>("");

        try {
            Optional<StatusPage> statusPage = StatuspageService.instance().get(incidentUpdateParams.getProject());
            if (!statusPage.isPresent()) {
                resultCode.set(Response.Status.NOT_FOUND.getStatusCode());
                resultMessage.set("could not find StatusPage object for project " + incidentUpdateParams.project);
            } else {
                Optional<Incident> incident = Optional.empty();
                try {
                    incident = statusPage.get().getIncident(incidentUpdateParams.page, incidentUpdateParams.incident, true);
                } catch (ServiceCallException e) {
//                    System.out.println("---> could not find incident on statuspage: " +
//                                       incidentUpdateParams.page +
//                                       " : " +
//                                       incidentUpdateParams.incident);
                }
                if (!incident.isPresent()) {
                    resultCode.set(Response.Status.NOT_FOUND.getStatusCode());
                    resultMessage.set("could not find incident " + incidentUpdateParams.getIncident() + " for page " + incidentUpdateParams.getPage());
                } else {
                    incident.get().status(IncidentStatus.of(incidentUpdateParams.getStatus()));
                    incident.get().impact(IncidentSeverity.of(incidentUpdateParams.getImpact()));
                    incident.get().components(getAffectedComponents(statusPage.get(), incidentUpdateParams));

                    if (StringUtils.isNotBlank(incidentUpdateParams.getMessage())) {
                        messageEscaped = org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(incidentUpdateParams.getMessage());
                    } else {
                        messageEscaped = Common.getDefaultStatusMessage(IncidentStatus.of(incidentUpdateParams.getStatus()));
                    }

                    Optional<Incident> updated = statusPage.get().updateIncident(incident.get(), messageEscaped);
                    if (updated.isPresent()
                            && updated.get().id().equals(incident.get().id())
                            && updated.get().impact() == incident.get().impact()
                            && updated.get().status() == incident.get().status()
                    ) {
                        resultStatus.set(true);
                        resultCode.set(Response.Status.OK.getStatusCode());
                    } else {
                        resultMessage.set("incident update error");
                    }
                }
            }
        } catch (ServiceCallException e) {
            resultMessage.set("StatusPage service error: "+ e.getMessage());
            resultCode.set(e.getCode());
        } catch (StatusPageException e) {
            resultMessage.set("StatusPage API error: " + e.getMessage());
        } catch (Exception e) {
            resultMessage.set(e.getMessage());
        }

//        System.out.println("-- rest: " + resultCode.get() + " " + resultStatus.get() + " " + resultMessage.get());

        if (resultStatus.get()) {
            if (incidentUpdateParams.publishComment && StringUtils.isNotBlank(messageEscaped)) {
                try {
                    CommentManager commentManager = ComponentAccessor.getCommentManager();
                    commentManager.create(
                        ComponentAccessor.getIssueManager().getIssueByKeyIgnoreCase(incidentUpdateParams.issue),
                        ComponentAccessor.getUserManager().getUserByName(request.getRemoteUser()),
                        messageEscaped,
                        true
                    );
                } catch (Exception e) {
                    System.err.println("could not publish message comment to Jira: " + e.getClass().getSimpleName() + " : " + e.getMessage());
                }
            }
            return Response.ok().build();
        } else {
            return Response.status(resultCode.get()).entity(resultMessage.get()).build();
        }
    }

    @GET
    @Path("/api/impacts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response impacts(@Context HttpServletRequest request) {
        return Response.ok(JiraTools.instance().getGsonObject().toJson(
            Arrays.stream(IncidentSeverity.values())
                .sorted(Comparator.comparing(IncidentSeverity::id))
                .map(IncidentSeverity::value)
                .collect(Collectors.toList())
        )).build();
    }
    @GET
    @Path("/api/component/statuses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response componentsStatuses(
            @Context HttpServletRequest request) {
        return Response.ok(JiraTools.instance().getGsonObject().toJson(
            Arrays.stream(ComponentStatus.values())
                .sorted(Comparator.comparing(ComponentStatus::id))
                .filter(c -> StringUtils.isNotBlank(c.value()))
                .map(AffectedComponentStatus::of)
                .collect(Collectors.toList())
        )).build();
    }

    private List<Component> getAffectedComponents(StatusPage statusPage, IncidentUpdateParams incidentUpdateParams) {

        Map<String, Component> pageComponents =
            statusPage.components(incidentUpdateParams.getPage(), true)
                .stream()
                .collect(Collectors.toMap(Component::id, c -> c));

        List<Component> result = new ArrayList<>();

        incidentUpdateParams.getComponents().keySet().stream().forEach(status -> {
            ComponentStatus componentStatus = ComponentStatus.of(status);
            incidentUpdateParams.getComponents().get(status).stream().forEach(componentId -> {
                Component component = pageComponents.get(componentId);
                if (null != component) {
                    component.status(componentStatus);
                }
                result.add(component);
            });
        });

        return result;
    }
    private List<Project> getConfiguredProjects(String sourceStr) {
        return Arrays.asList(sourceStr.split(","))
            .stream()
            .map(String::trim)
            .distinct()
            .map(JiraTools.instance()::getProjectByKey)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())
        ;
    }

}
