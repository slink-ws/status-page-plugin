package ws.slink.statuspage.model;

import ws.slink.statuspage.type.IncidentStatus;

public class IssueIncidentStatus {

    public String id;
    public String title;

    public String id() {
        return id;
    }
    public String title() {
        return title;
    }

    public static IssueIncidentStatus of(IncidentStatus status) {
        IssueIncidentStatus result = new IssueIncidentStatus();
        result.id = status.value();
        result.title = status.value().replaceAll("_", " ").toLowerCase();
        return result;
    }

}
