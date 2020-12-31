package ut.ws.slink;

import org.junit.Test;
import ws.slink.api.StatuspagePluginComponent;
import ws.slink.impl.StatuspagePluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class StatuspageComponentUnitTest
{
    @Test
    public void testMyName()
    {
        StatuspagePluginComponent component = new StatuspagePluginComponentImpl(null);
        assertEquals("names do not match!", "statuspageComponent",component.getName());
    }
}