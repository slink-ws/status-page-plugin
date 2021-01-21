package ws.slink.statuspage.model;

import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.type.ComponentStatus;

public class AffectedComponentStatus {

    public String id;
    public String title;

    public String id() {
        return id;
    }
    public String title() {
        return title;
    }

    public static AffectedComponentStatus of(ComponentStatus status) {
        AffectedComponentStatus result = new AffectedComponentStatus();
        result.id = status.value();
        result.title = StringUtils.capitalize(status.value().replaceAll("_", " "));
        return result;
    }

}
