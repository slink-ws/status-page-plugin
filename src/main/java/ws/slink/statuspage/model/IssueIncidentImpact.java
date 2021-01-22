package ws.slink.statuspage.model;

import ws.slink.statuspage.type.IncidentSeverity;

public class IssueIncidentImpact {

    public String id;
    public String title;

    public String id() {
        return id;
    }
    public String title() {
        return title;
    }

    public static IssueIncidentImpact of(IncidentSeverity status) {
        IssueIncidentImpact result = new IssueIncidentImpact();
        result.id = status.value();
        result.title = status.value().replaceAll("_", " ").toLowerCase();
        return result;
    }

}
