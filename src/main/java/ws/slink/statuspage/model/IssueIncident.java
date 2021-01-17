package ws.slink.statuspage.model;

import java.time.LocalDateTime;

public class IssueIncident {

    private String pageId;
    private String incidentId;
    private String createdBy;
    private String linkedBy;
    private LocalDateTime createdAt;
    private LocalDateTime linkedAt;

    private Incident incident;

    public void sync() {

    }

}
