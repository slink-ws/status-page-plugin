package ws.slink.statuspage.impl;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import ws.slink.statuspage.api.StatuspagePluginComponent;

import javax.inject.Inject;
import javax.inject.Named;

@ExportAsService ({StatuspagePluginComponent.class})
@Named ("statuspagePluginComponent")
public class StatuspagePluginComponentImpl implements StatuspagePluginComponent {
        @ComponentImport
        private final ApplicationProperties applicationProperties;

        @Inject
        public StatuspagePluginComponentImpl(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
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