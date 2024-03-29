package ws.slink.statuspage.servlet;

import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import ws.slink.statuspage.model.ConfigRole;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.tools.JiraTools;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Scanned
public class ConfigServlet extends HttpServlet {

    @ComponentImport private final UserManager userManager;
    @ComponentImport private final TemplateRenderer renderer;
//    @ComponentImport private final LoginUriProvider loginUriProvider;

    @Inject
    public ConfigServlet(UserManager userManager, /*LoginUriProvider loginUriProvider,*/ TemplateRenderer renderer) {
        this.userManager = userManager;
//        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String [] parts = request.getRequestURL().toString().split("/");
        Map<String, Object> contextParams = new HashMap<>();
        if (parts.length > 1) {
            Project project = JiraTools.instance().getProjectByKey(parts[parts.length - 2]);
            if (null != project) {
                contextParams.put("projectKey", project.getKey());
                contextParams.put("projectId", project.getId());
            }

            Collection<String> selectedMgmtRolesParam  = ConfigService.instance().getConfigMgmtRoles(project.getKey());
            Collection<String> selectedViewRolesParam  = ConfigService.instance().getConfigViewRoles(project.getKey());

            ProjectRoleManager projectRoleManager = ComponentManager.getInstance().getComponentInstanceOfType(ProjectRoleManager.class);
            Collection<ConfigRole>      mgmtRoles = projectRoleManager.getProjectRoles()
                .stream()
                .map(role -> new ConfigRole(role, selectedMgmtRolesParam.contains(String.valueOf(role.getId()))))
                .collect(Collectors.toList());
            Collection<ConfigRole>      viewRoles = projectRoleManager.getProjectRoles()
                .stream()
                .map(role -> new ConfigRole(role, selectedViewRolesParam.contains(String.valueOf(role.getId()))))
                .collect(Collectors.toList());

            contextParams.put("managers", mgmtRoles);
            contextParams.put("viewers" , viewRoles);
            contextParams.put("apiKey"  , ConfigService.instance().getConfigApiKey(project.getKey()));

            if (!JiraTools.instance().isPluginManager(userManager.getRemoteUser())) {
                response.setContentType("text/html;charset=utf-8");
                renderer.render("templates/unauthorized.vm", contextParams, response.getWriter());
            } else {
                // System.out.println("CONTEXT PARAMS: " + contextParams);
                response.setContentType("text/html;charset=utf-8");
                renderer.render("templates/config.vm", contextParams, response.getWriter());
            }
        }
    }
}