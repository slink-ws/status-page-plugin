package ws.slink.statuspage.service;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigService {

    private static class PluginConfigServiceSingleton {
        private static final ConfigService INSTANCE = new ConfigService();
    }
    public static ConfigService instance () {
        return PluginConfigServiceSingleton.INSTANCE;
    }

    public static final String CONFIG_PREFIX         = "ws.slink.status-page-plugin";
    public static final String CONFIG_ADMIN_PROJECTS = "admin.projects";
    public static final String CONFIG_ADMIN_ROLES    = "admin.roles";

    private PluginSettings pluginSettings;

    private ConfigService(){}

    public void setPluginSettings(PluginSettings pluginSettings) {
        if (null == this.pluginSettings)
            this.pluginSettings = pluginSettings;
    }

    public Collection<String> getProjects() { // returns list of projectKey
        return getListParam(CONFIG_ADMIN_PROJECTS);
    }
    public Collection<String> getRoles() { // returns list of roleId
        return getListParam(CONFIG_ADMIN_ROLES);
    }
    public void setProjects(String projects) {
        pluginSettings.put(CONFIG_PREFIX + "." + CONFIG_ADMIN_PROJECTS, projects);
    }
    public void setRoles(String roles) {
        pluginSettings.put(CONFIG_PREFIX + "." + CONFIG_ADMIN_ROLES, roles);
    }

    private List<String> getListParam(String param) {
        String value = (String) pluginSettings.get(CONFIG_PREFIX + "." + param);
        if (StringUtils.isBlank(value))
            return Collections.EMPTY_LIST;
        else
            return Arrays.stream(value.split(",")).map(s -> s.trim()).collect(Collectors.toList());
    }


/*
    // --- administration
    public String getRoles() {
        return (String) pluginSettings.get(CONFIG_PREFIX + ".roles");
    }
    public String getProjects() {
        return (String) pluginSettings.get(CONFIG_PREFIX + ".projects");
    }
    public List<String> rolesList() {
        return getListParam("roles");
    }
    public List<String> projectsList() {
        return getListParam("projects");
    }

    // --- configuration
    public String getList(String projectKey, int id) {
        return getConfigValue(projectKey, ".list" + id);
    }
    public void setList(String projectKey, int id, String value) {
        setConfigValue(projectKey, ".list" + id, setString(value, "", ""));
    }
    public String getStyle(String projectKey, int id) {
        return getConfigValue(projectKey, ".style" + id);
    }
    public void setStyle(String projectKey, int id, String value) {
        setConfigValue(projectKey, ".style" + id, value);
    }
    public String getText(String projectKey, int id) {
        return getConfigValue(projectKey, ".text" + id);
    }
    public void setText(String projectKey, int id, String value) {
        setConfigValue(projectKey, ".text" + id, setString(value, "", ""));
    }
    public String getViewers(String projectKey) {
        return getConfigValue(projectKey, ".viewers");
    }
    public void setViewers(String projectKey, String value) {
        setConfigValue(projectKey, ".viewers", setString(value, "", ""));
    }
    public String getColor(String projectKey, int id) {
        return getConfigValue(projectKey, ".color" + id);
    }
    public void setColor(String projectKey, int id, String value) {
        if (value.startsWith("#"))
            setConfigValue(projectKey, ".color" + id, value);
        else
            setConfigValue(projectKey, ".color" + id, "#" + value.trim());
    }

    // --- tools
    private String setString(String value, String defaultValue, String newLineReplacement) {
        if (null == value || value.isEmpty())
            return defaultValue;
        else
            return value
                .replaceAll(" +", " ")
                .replaceAll(";" , " ")
                .replaceAll("," , " ")
                .replaceAll("\n", newLineReplacement);
    }
    private String getConfigValue(String projectKey, String key) {
        String result = (StringUtils.isBlank(projectKey))
                      ? (String) pluginSettings.get(CONFIG_PREFIX + key)
                      : (String) pluginSettings.get(CONFIG_PREFIX + "." + projectKey + key);
        return StringUtils.isBlank(result) ? "" : result;
    }
    private void setConfigValue(String projectKey, String key, String value) {
        String cfgKey = (StringUtils.isBlank(projectKey))
                      ? CONFIG_PREFIX + "key"
                      : CONFIG_PREFIX + "." + projectKey + key;
        pluginSettings.put(cfgKey, value);
    }
*/

}
