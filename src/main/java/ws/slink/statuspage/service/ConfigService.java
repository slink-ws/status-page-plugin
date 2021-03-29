package ws.slink.statuspage.service;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private static class PluginConfigServiceSingleton {
        private static final ConfigService INSTANCE = new ConfigService();
    }
    public static ConfigService instance () {
        return PluginConfigServiceSingleton.INSTANCE;
    }

    public static final String CONFIG_PREFIX                  = "ws.slink.status-page-plugin";

    public static final String CONFIG_ADMIN_PROJECTS          = "admin.projects";
    public static final String CONFIG_ADMIN_ROLES             = "admin.roles";
    public static final String CONFIG_ADMIN_CUSTOM_FIELD_NAME = "admin.custom_field_name";
    public static final String CONFIG_MGMT_ROLES              = "config.mgmt.roles";
    public static final String CONFIG_VIEW_ROLES              = "config.view.roles";
    public static final String CONFIG_API_KEY                 = "config.api.key";

    private static final String DEFAULT_CUSTOM_FIELD_ID       = "status-page-incident";

    private PluginSettings pluginSettings;

    private ConfigService() {
//        System.out.println("---- created config service");
    }

    synchronized public void init(PluginSettingsFactory pluginSettingsFactory) {
        if (null == this.pluginSettings)
//            this.pluginSettings = pluginSettingsFactory.createSettingsForKey(CONFIG_PREFIX);
            this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    public Collection<String> getAdminProjects() { // returns list of projectKey
        return getListParam(CONFIG_ADMIN_PROJECTS);
    }
    public void setAdminProjects(String projects) {
        setParam(CONFIG_ADMIN_PROJECTS, projects);
    }

    public Collection<String> getAdminRoles() { // returns list of roleId
        return getListParam(CONFIG_ADMIN_ROLES);
    }
    public void setAdminRoles(String roles) {
        setParam(CONFIG_ADMIN_ROLES, roles);
    }

    public String getAdminCustomFieldName() {
        String result = getParam(CONFIG_ADMIN_CUSTOM_FIELD_NAME);
        if (StringUtils.isBlank(result))
            return DEFAULT_CUSTOM_FIELD_ID;
        else
            return result;
    }
    public void setAdminCustomFieldName(String value) {
        setParam(CONFIG_ADMIN_CUSTOM_FIELD_NAME, value);
    }

    public Collection<String> getConfigMgmtRoles(String projectKey) {
        return getListParam(CONFIG_MGMT_ROLES, projectKey);
    }
    public void setConfigMgmtRoles(String projectKey, String roles) {
        setParam(CONFIG_MGMT_ROLES, projectKey, roles);
    }

    public Collection<String> getConfigViewRoles(String projectKey) {
        return getListParam(CONFIG_VIEW_ROLES, projectKey);
    }
    public void setConfigViewRoles(String projectKey, String roles) {
        setParam(CONFIG_VIEW_ROLES, projectKey, roles);
    }

    public String getConfigApiKey(String projectKey) {
        return getParam(CONFIG_API_KEY, projectKey);
    }
    public void setConfigApiKey(String projectKey, String value) {
        setParam(CONFIG_API_KEY, projectKey, value);
    }

    private void setParam(String paramKey, String value) {
        setParam(paramKey, null, value);
    }
    private void setParam(String paramKey, String projectKey, Object value) {
        String key = CONFIG_PREFIX + "." + paramKey;
        if (StringUtils.isNotBlank(projectKey)) {
            key += "." + projectKey;
        }
        log.trace("--- [STATUSPAGE] [CONFIG] SET KEY {} : {}", key, value);
        pluginSettings.put(key, value);
    }

    private String getParam(String paramKey) {
        return getParam(paramKey, null);
    }
    private String getParam(String paramKey, String projectKey) {
        String key = CONFIG_PREFIX + "." + paramKey;
        if (StringUtils.isNotBlank(projectKey)) {
            key += "." + projectKey;
        }
        String value = (String) pluginSettings.get(key);
        log.trace("--- [STATUSPAGE] [CONFIG] GET KEY {}.{} : empty", CONFIG_PREFIX, key);
        if (StringUtils.isBlank(value))
            return "";
        else
            return value;
    }

    private List<String> getListParam(String paramKey) {
        return getListParam(paramKey, null);
    }
    private List<String> getListParam(String paramKey, String projectKey) {
        try {
            String value = getParam(paramKey, projectKey);
            if (StringUtils.isBlank(value)) {
                log.trace("--- [STATUSPAGE] [CONFIG] GET LIST {} : null", paramKey);
                return Collections.EMPTY_LIST;
            } else {
                List<String> result = Arrays.stream(value.split(","))
                    .map(s -> s.trim())
                    .collect(Collectors.toList());
                log.trace("--- [STATUSPAGE] [CONFIG] GET LIST {} : {}", paramKey, result);
                return result;
            }
        } catch (Exception e) {
            log.trace("--- [STATUSPAGE] [CONFIG] GET LIST {} : empty", paramKey);
            return Collections.EMPTY_LIST;
        }
    }
}
