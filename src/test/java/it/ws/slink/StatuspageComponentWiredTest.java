package it.ws.slink;

import org.junit.Test;
import org.junit.runner.RunWith;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import ws.slink.api.StatuspagePluginComponent;
import com.atlassian.sal.api.ApplicationProperties;

import static org.junit.Assert.assertEquals;

@RunWith(AtlassianPluginsTestRunner.class)
public class StatuspageComponentWiredTest
{
    private final ApplicationProperties applicationProperties;
    private final StatuspagePluginComponent myPluginComponent;

    public StatuspageComponentWiredTest(ApplicationProperties applicationProperties, StatuspagePluginComponent myPluginComponent)
    {
        this.applicationProperties = applicationProperties;
        this.myPluginComponent = myPluginComponent;
    }

    @Test
    public void testMyName()
    {
        assertEquals("names do not match!", "statuspageComponent:" + applicationProperties.getDisplayName(),myPluginComponent.getName());
    }
}