package ws.slink.statuspage.servlet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.StatusPage;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.JiraTools;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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

        transactionTemplate.execute((TransactionCallback) () -> {
//            config.log("~~~ received configuration: ");

            if (CustomFieldService.instance().isExists(config.getCustomFieldId())) {
                if (CustomFieldService.instance().isCorrectType(config.getCustomFieldId(), CustomFieldService.INCIDENT_CUSTOM_FIELD_KEY)) {
                    System.out.println("custom field exists; just save");
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
                    CustomFieldService.INCIDENT_CUSTOM_FIELD_DESCRIPTION
                )) {
                    saveResult.set(false);
                    message.set("could not create custom field '" + config.getCustomFieldId() + "'");
                };
            }

            if (saveResult.get()) {
                ConfigService.instance().setAdminProjects(config.getProjects());
                ConfigService.instance().setAdminRoles(config.getRoles());
                ConfigService.instance().setAdminCustomFieldId(config.getCustomFieldId());
                StatuspageService.instance().clear();
                ConfigService.instance().getAdminProjects().stream().forEach(p ->
                    StatuspageService.instance().init(p, ConfigService.instance().getConfigApiKey(p))
                );
            }

/*
            System.out.println("--- available custom fields ----------------------");
            ComponentAccessor.getComponent(CustomFieldManager.class)
                .getCustomFieldObjects()
                .stream()
                .map(CustomField::getName)
                .forEach(System.out::println)
            ;
            System.out.println("--------------------------------------------------");
            Optional<CustomField> existingIncidentCustomField =
                ComponentAccessor
                    .getComponent(CustomFieldManager.class)
                    .getCustomFieldObjectsByName(ConfigService.instance().getAdminCustomFieldId())
                    .stream()
                    .filter(cf -> cf.getFieldName().equals(ConfigService.instance().getAdminCustomFieldId()))
                    .findAny();
            if (existingIncidentCustomField.isPresent()) {
                System.out.println(
                    "found custom field with name " +
                    ConfigService.instance().getAdminCustomFieldId() +
                    " of type '" +
                    existingIncidentCustomField.get().getCustomFieldType().getName() +
                    "'"
                );
                System.out.println("type key         :" + existingIncidentCustomField.get().getCustomFieldType().getKey());
                System.out.println("type cit         :" +
                    existingIncidentCustomField.get().getCustomFieldType().getConfigurationItemTypes());
                System.out.println("type name        :" + existingIncidentCustomField.get().getCustomFieldType().getName());
                System.out.println("type description :" + existingIncidentCustomField.get().getCustomFieldType().getDescription());
                System.out.println("type descriptor  :" + existingIncidentCustomField.get().getCustomFieldType().getDescriptor());
            } else {
                System.out.println("not found custom field with name " + ConfigService.instance().getAdminCustomFieldId());
            }
            System.out.println("--------------------------------------------------");
*/
//            status-page-incident

//            CustomFieldType cft =  ComponentAccessor.getComponent(CustomFieldManager.class).getCustomFieldType("status-page-incident");
//                .getCustomFieldType("ws.slink.statuspage.customfield:incidentcustomfield");
//            System.out.println("custom field type: " + cft);

//            ComponentAccessor.getComponent(CustomFieldManager.class)
//                .createCustomField("incident", "statuspage incident assigned to the issue", IncidentCustomField.class, null, null, null)

//            CustomFieldUtils.

//            FieldConfigScheme newConfigScheme = new FieldConfigScheme.Builder()
//                .setName("incident")
//                .setDescription("statuspage incident assigned to the issue")
//                .setFieldId(ConfigService.instance().getAdminCustomFieldId())
//                .toFieldConfigScheme()
//            ;
//
//            System.out.println("   id:" + newConfigScheme.getId());
//            System.out.println(" name:" + newConfigScheme.getName());
//            System.out.println("descr:" + newConfigScheme.getDescription());
//            System.out.println("  cfg:" + newConfigScheme.getOneAndOnlyConfig());

//            System.out.println(new Gson().toJson(newConfigScheme));


//            ComponentAccessor.getComponent(FieldConfigSchemeManager.class)
//                .getConfigSchemesForField(newConfigScheme.getField())
//                .stream()
//                .map(cs -> cs.getName())
//                .forEach(System.out::println)
//            ;
//                    .createFieldConfigScheme(newConfigScheme, contexts, issueTypes, field);
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
        if (!JiraTools.isPluginManager(userManager.getRemoteUser())) {
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(new Gson().toJson(statusPage.get().pages())).build();
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(new Gson().toJson(statusPage.get().components(pageId))).build();
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(new Gson().toJson(statusPage.get().groups(pageId))).build();
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(new Gson().toJson(statusPage.get().groupComponents(pageId, groupId))).build();
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        return Response.ok(new Gson().toJson(statusPage.get().nonGroupComponents(pageId))).build();
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

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.noContent().build();

        if (!JiraTools.isIncidentManager(
                project.get().getKey(),
                ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            System.out.println("--- UNAUTHORIZED");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        if (activeOnly) {
            return Response.ok(new Gson().toJson(
                statusPage.get().incidents(pageId)
                    .stream()
                    .filter(
                        i -> i.status() != IncidentStatus.RESOLVED
                          && i.status() != IncidentStatus.COMPLETED
                    ).collect(Collectors.toList())
            )).build();
        } else {
            return Response.ok(new Gson().toJson(statusPage.get().incidents(pageId))).build();
        }
    }

    /*
    @POST
    @Path("/api/incident/link")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response linkIncident(
            @QueryParam("issueKey") String issueKey,
            @QueryParam("pageId") String pageId,
            @QueryParam("incidentId") String incidentId,
            @Context HttpServletRequest request) {

        if (StringUtils.isBlank(issueKey))
            return Response.status(Response.Status.BAD_REQUEST).build();

        if (StringUtils.isBlank(pageId))
            return Response.status(Response.Status.BAD_REQUEST).build();

        if (StringUtils.isBlank(incidentId))
            return Response.status(Response.Status.BAD_REQUEST).build();

        Optional<Project> project = JiraTools.getProjectForIssue(issueKey);
        if (!project.isPresent())
            return Response.status(Response.Status.BAD_REQUEST).build();

        if (!JiraTools.isIncidentManager(
            project.get().getKey(),
            ComponentAccessor.getUserManager().getUserByName(userManager.getRemoteUser().getUsername()))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        Optional<StatusPage> statusPage = StatuspageService.instance().get(project.get().getKey());
        if (!statusPage.isPresent())
            return Response.noContent().build();

        Optional<Issue> issue = JiraTools.getIssueByKey(issueKey);

        System.out.println("--- link incident:      issue - " + issue);
        System.out.println("--- link incident:     pageId - " + pageId);
        System.out.println("--- link incident: incidentId - " + incidentId);

//        issue.get().getCustomFieldValue(CustomField)

//        return Response.ok(new Gson().toJson(statusPage.get().nonGroupComponents(pageId))).build();
        return Response.noContent().build();
    }
    */

}
