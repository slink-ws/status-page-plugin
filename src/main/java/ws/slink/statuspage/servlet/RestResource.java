package ws.slink.statuspage.servlet;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.tools.JiraTools;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
        public String toString() {
            return projects + " : " + roles;
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

/*
    @GET
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAdminParams(@Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUser().getUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(transactionTemplate.execute((TransactionCallback) () -> new AdminParams()
            .setProjects(ConfigService.instance().getProjects())
            .setRoles(ConfigService.instance().getRoles())
//            .log("~~~ prepared configuration: ")
        )).build();
    }
*/

    @PUT
    @Path("/admin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putAdminParams(final AdminParams config, @Context HttpServletRequest request) {
        UserKey userKey = userManager.getRemoteUser().getUserKey();
        if (userKey == null || !userManager.isSystemAdmin(userKey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        transactionTemplate.execute((TransactionCallback) () -> {
//            config.log("~~~ received configuration: ");
            ConfigService.instance().setAdminProjects(config.getProjects());
            ConfigService.instance().setAdminRoles(config.getRoles());
            return null;
        });
        return Response.noContent().build();
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
            return null;
        });
        return Response.noContent().build();
    }

    /*
    @GET
    @Path("/config/{projectKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfigParams(@PathParam("projectKey") String projectKey, @Context HttpServletRequest request) {
        if (!JiraTools.isPluginManager(userManager.getRemoteUser()))
            return Response.status(Response.Status.UNAUTHORIZED).build();
        else
            return Response.ok(transactionTemplate.execute((TransactionCallback) () ->
                new ConfigParams()
                .setViewers(ConfigService.instance().getViewers(projectKey))
                .setList1(ConfigService.instance().getList(projectKey, 1))
                    .setStyle1(ConfigService.instance().getStyle(projectKey, 1))
                        .setText1(ConfigService.instance().getText(projectKey, 1))
                            .setColor1(ConfigService.instance().getColor(projectKey, 1))
                .setList2(ConfigService.instance().getList(projectKey, 2))
                    .setStyle2(ConfigService.instance().getStyle(projectKey, 2))
                        .setText2(ConfigService.instance().getText(projectKey, 2))
                            .setColor2(ConfigService.instance().getColor(projectKey, 2))
                .setList3(ConfigService.instance().getList(projectKey, 3))
                    .setStyle3(ConfigService.instance().getStyle(projectKey, 3))
                        .setText3(ConfigService.instance().getText(projectKey, 3))
                            .setColor3(ConfigService.instance().getColor(projectKey, 3))
                .setList4(ConfigService.instance().getList(projectKey, 4))
                    .setStyle4(ConfigService.instance().getStyle(projectKey, 4))
                        .setText4(ConfigService.instance().getText(projectKey, 4))
                            .setColor4(ConfigService.instance().getColor(projectKey, 4))
//                .log("~~~ prepared configuration: \n")
            )).build();
    }

    @PUT
    @Path("/config/{projectKey}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putConfigParams(final ConfigParams config, @PathParam("projectKey") String projectKey, @Context HttpServletRequest request) {
        if (!JiraTools.isPluginManager(userManager.getRemoteUser())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        transactionTemplate.execute((TransactionCallback) () -> {
//            config.log("~~~ received configuration: \n");
            ConfigService.instance().setList(projectKey,1, config.getList1());
            ConfigService.instance().setList(projectKey,2, config.getList2());
            ConfigService.instance().setList(projectKey,3, config.getList3());
            ConfigService.instance().setList(projectKey,4, config.getList4());

            ConfigService.instance().setStyle(projectKey,1, config.getStyle1());
            ConfigService.instance().setStyle(projectKey,2, config.getStyle2());
            ConfigService.instance().setStyle(projectKey,3, config.getStyle3());
            ConfigService.instance().setStyle(projectKey,4, config.getStyle4());

            ConfigService.instance().setText(projectKey,1, config.getText1());
            ConfigService.instance().setText(projectKey,2, config.getText2());
            ConfigService.instance().setText(projectKey,3, config.getText3());
            ConfigService.instance().setText(projectKey,4, config.getText4());

            ConfigService.instance().setColor(projectKey,1, config.getColor1());
            ConfigService.instance().setColor(projectKey,2, config.getColor2());
            ConfigService.instance().setColor(projectKey,3, config.getColor3());
            ConfigService.instance().setColor(projectKey,4, config.getColor4());

            ConfigService.instance().setViewers(projectKey, config.getViewers());

            return null;
        });
        return Response.noContent().build();
    }

    @GET
    @Path("/color/{issueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIssueColor(@PathParam("issueId") String issueId, @Context HttpServletRequest request) {
//        System.out.print("color request for '" + issueId + "': ");
        try {
            ApplicationUser user = JiraTools.getLoggedInUser();
            Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueId); //((Issue) new JiraHelper().getContextParams().get("issue"));
            if (null == user || null == issue) {
                // System.out.println("none (1)");
                return emptyResponse();
            }

//            System.out.println("~~~~~~ PROJECT: " + issue.getProjectObject().getKey()
//                             + "; CONFIGURED PROJECTS: " + ConfigService.instance().projectsList()
//                             + "; VIEWER '" + user.getName() + "': " + JiraTools.isViewer(issue.getProjectObject().getKey(), user)
//            );

            if (JiraTools.isViewer(issue.getProjectObject().getKey(), user) && ConfigService.instance().projectsList().contains(issue.getProjectObject().getKey())){
                String reporterEmail = issue.getReporter().getEmailAddress();
                String color = ConfigService.instance().getColor(issue.getProjectObject().getKey(), CustomerLevelService.getLevel(issue.getProjectObject().getKey(), reporterEmail));
                if (StringUtils.isNotBlank(color)) {
                    // System.out.println(color);
                    return createColorResponse(color);
                }
                else {
                    // System.out.println("none (2)");
                    return emptyResponse();
                }
            } else {
                // System.out.println("none (3)");
                return emptyResponse();
            }
        } catch (Exception e) {
            // System.out.println("error: " + e.getClass().getName() + " : " + e.getMessage());
            return Response.serverError().build();
        }
    }

    private Response createColorResponse(String color) {
        return Response.ok(transactionTemplate.execute((TransactionCallback) () ->
            new ColorParams()
                .setColor(color
                )
//                .log("REST COLOR REQUEST: ~~~ prepared color: \n")
            )
        ).build();
    }

    private Response emptyResponse() {
        return Response.noContent().build();
    }

     */
}

