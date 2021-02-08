package ws.slink.statuspage.tools;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.sal.api.user.UserProfile;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.json.CustomExclusionStrategy;
import ws.slink.statuspage.service.ConfigService;
import ws.slink.statuspage.service.CustomFieldService;
import ws.slink.statuspage.service.StatuspageService;

import java.util.*;
import java.util.stream.Collectors;

public class JiraTools {

    private static class JiraToolsSingleton {
        private static final JiraTools INSTANCE = new JiraTools();
    }
    public static JiraTools instance () {
        return JiraTools.JiraToolsSingleton.INSTANCE;
    }

    public boolean isPluginManager(UserProfile user) {
        return isPluginManager(user.getUsername());
    }
    public boolean isPluginManager(ApplicationUser user) {
        return isPluginManager(user.getUsername());
    }
    private boolean isPluginManager(String user) {
        Project currentProject = ComponentAccessor.getProjectManager().getProjectObj(new ProjectActionSupport().getSelectedProjectId());

        boolean canManage = (null != currentProject
            && ConfigService.instance().getAdminProjects().contains(currentProject.getKey())
            && userHasRoles(
                currentProject,
                ConfigService.instance().getAdminRoles().stream()
                    .map(Long::valueOf)
                    .map(this::getProjectRoleById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                ComponentAccessor.getUserManager().getUserByName(user))
        );
//        System.out.println(user + ((canManage) ? " can " : " can't ") + "manage " + currentProject.getKey());
        return canManage;


    }

    public boolean isIncidentManager(Project project, ApplicationUser applicationUser) {
        return ConfigService.instance().getConfigMgmtRoles(project.getKey()).stream().map(Long::valueOf).anyMatch(r ->
            userHasRoleInProject(
                project,
                applicationUser,
                getProjectRoleById(r)
            )
        );
    }
    public boolean isIncidentViewer(Project project, ApplicationUser applicationUser) {
        return ConfigService.instance().getConfigViewRoles(project.getKey()).stream().map(Long::valueOf).anyMatch(r ->
            userHasRoleInProject(
                project,
                applicationUser,
                getProjectRoleById(r)
            )
        );
    }
    public boolean isIncidentManager(String projectKey, ApplicationUser applicationUser) {
        return isIncidentManager(getProjectByKey(projectKey), applicationUser);
    }
    public boolean isIncidentViewer(String projectKey, ApplicationUser applicationUser) {
        return isIncidentViewer(getProjectByKey(projectKey), applicationUser);
    }
    public boolean isIncidentManager(Long projectId, ApplicationUser applicationUser) {
        return isIncidentManager(getProjectById(projectId), applicationUser);
    }
    public boolean isIncidentViewer(Long projectId, ApplicationUser applicationUser) {
        return isIncidentViewer(getProjectById(projectId), applicationUser);
    }

    public boolean isIncidentExists(Issue issue) {
        return StatuspageService.instance().getIncident(issue).isPresent();
    }

    public boolean isIncidentsGlobalConfigReady() {
        return ConfigService.instance().getAdminProjects().size() > 0
            && ConfigService.instance().getAdminRoles().size()    > 0
            && StringUtils.isNotBlank(ConfigService.instance().getAdminCustomFieldName())
            && CustomFieldService.instance().exists(ConfigService.instance().getAdminCustomFieldName())
            && CustomFieldService.instance().correct(ConfigService.instance().getAdminCustomFieldName(), CustomFieldService.INCIDENT_CUSTOM_FIELD_KEY)
        ;
    }

    public boolean isIncidentsProjectConfigReady(Project project) {
        return ConfigService.instance().getConfigMgmtRoles(project.getKey()).size() > 0
            && StringUtils.isNotBlank(ConfigService.instance().getConfigApiKey(project.getKey()))
        ;
    }

    public boolean isIncidentsEnabled(Project project) {
        return ConfigService.instance().getAdminProjects().contains(project.getKey());
    }
    public boolean isIncidentsEnabled(Long projectId) {
        return isIncidentsEnabled(getProjectById(projectId));
    }
    public boolean isIncidentsEnabled(String projectKey) {
        return isIncidentsEnabled(getProjectByKey(projectKey));
    }

    public boolean userHasRolesInProjects(Collection<Project> projects, Collection<ProjectRole> roles, ApplicationUser user) {
        return projects.stream().filter(Objects::nonNull).anyMatch(p -> roles.stream().filter(Objects::nonNull).anyMatch(r -> userHasRoleInProject(p, user, r)));
    }
    public boolean userHasRoles(Project project, List<ProjectRole> roles, ApplicationUser user) {
        return userHasRolesInProjects(Arrays.asList(project), roles, user);
    }
    public boolean userHasRoleInProject(Project project, ApplicationUser user, ProjectRole role) {
        ProjectRoleManager projectRoleManager = ComponentAccessor.getComponentOfType(ProjectRoleManager.class);
        boolean result = projectRoleManager.isUserInProjectRole(user, role, project);
        // System.out.println(user.getUsername() + " : " + project.getKey() + " : " + role.getName() + " => " + result);
        return result;
    }

    public ProjectRole getProjectRoleById(Long roleId) {
        try {
            ProjectRole projectRole = ComponentAccessor.getComponent(ProjectRoleManager.class).getProjectRole(roleId);
            return projectRole;
        } catch (Exception e) {
//             System.err.println("ERROR CONVERTING ROLE ID TO PROJECT ROLE: " + e.getMessage());
            return null;
        }
    }
    public Project getProjectByKey(String projectKey) {
        try {
            return ComponentAccessor.getProjectManager().getProjectByCurrentKey(projectKey);
        } catch (Exception e) {
            // System.err.println("ERROR CONVERTING PROJECT KEY TO PROJECT: " + e.getMessage());
            return null;
        }
    }
    public Project getProjectById(Long projectId) {
        try {
            return ComponentAccessor.getProjectManager().getProjectObj(projectId);
        } catch (Exception e) {
            // System.err.println("ERROR CONVERTING PROJECT KEY TO PROJECT: " + e.getMessage());
            return null;
        }
    }
    public ApplicationUser getLoggedInUser() {
        JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        return jiraAuthenticationContext.getLoggedInUser();
    }
    public Optional<Project> getProjectForIssue(String issueKey) {
        if (StringUtils.isBlank(issueKey))
            return Optional.empty();
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey);
        if (null == issue)
            return Optional.empty();
        return Optional.ofNullable(this.getProjectById(issue.getProjectId()));
    }
    public Optional<Issue> getIssueByKey(String issueKey) {
        if (StringUtils.isBlank(issueKey))
            return Optional.empty();
        return Optional.ofNullable(ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey));
    }

    public boolean setCustomFieldValue(Issue issue, CustomField customField, Object value, boolean override) {
        customField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(customField), value), new DefaultIssueChangeHolder());
        return true;
/*
        Object cf = issue.getCustomFieldValue(customField);
        if (null != cf) {
            if (!override) {
                System.out.println(" ---> custom field value is not null");
                return false;
            } else {
                customField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(customField), value), new DefaultIssueChangeHolder());
                return true;
            }
        } else {
            IssueInputParameters issueInputParameters = new IssueInputParametersImpl();
            issueInputParameters.addCustomFieldValue(customField.getId(), value);
            IssueService.UpdateValidationResult updateValidationResult =
                    ComponentManager.getComponentInstanceOfType(IssueService.class)
                            .validateUpdate(getLoggedInUser(), issue.getId(), issueInputParameters);
            if (updateValidationResult.isValid()) {
                IssueService.IssueResult updateResult =
                        ComponentManager.getComponentInstanceOfType(IssueService.class)
                                .update(getLoggedInUser(), updateValidationResult);
                if (!updateResult.isValid()) {
                    System.out.println(" ---> unsuccessful update");
                    return false;
                } else {
                    System.out.println(" ---> successful update");
                    System.out.println("---> new CustomField value: " + issue.getCustomFieldValue(customField));
                    return true;
                }
            } else {
                System.out.println(" ---> updateValidationResult is invalid");
                return false;
            }
        }

 */
    }

    public Gson getGsonObject() {
        ExclusionStrategy strategy = new CustomExclusionStrategy();
        return new GsonBuilder()
            .addSerializationExclusionStrategy(strategy)
            .addDeserializationExclusionStrategy(strategy)
            .create()
        ;
    }

    public String getDateTimeFormat() {
//        String key = APKeys.JIRA_LF_DATE_TIME;
        String key = APKeys.JIRA_LF_DATE_COMPLETE;
        return ComponentManager.getInstance().getComponentInstanceOfType(ApplicationProperties.class).getDefaultBackedString(key);
    }
}
