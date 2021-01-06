package ws.slink.statuspage.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import ws.slink.statuspage.api.StatuspagePluginComponent;
import ws.slink.statuspage.service.ConfigService;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({StatuspagePluginComponent.class})
@Named ("statuspagePluginComponent")
public class StatuspagePluginComponentImpl implements StatuspagePluginComponent {

        @ComponentImport private final ApplicationProperties applicationProperties;
        @ComponentImport private final PluginSettingsFactory pluginSettingsFactory;

        @Inject
        public StatuspagePluginComponentImpl(final ApplicationProperties applicationProperties, final PluginSettingsFactory pluginSettingsFactory)
    {
        this.applicationProperties = applicationProperties;
        this.pluginSettingsFactory = pluginSettingsFactory;
        ConfigService.instance().setPluginSettings(pluginSettingsFactory.createGlobalSettings());
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "statuspageComponent:" + applicationProperties.getDisplayName();
        }
        
        return "statuspageComponent";
    }
}