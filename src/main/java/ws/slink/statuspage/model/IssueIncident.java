package ws.slink.statuspage.model;

import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;

public class IssueIncident {

    private String pageId;
    private String incidentId;
    private String createdBy;
    private String linkedBy;
    private LocalDateTime createdAt;
    private LocalDateTime linkedAt;

    @Expose(serialize = false, deserialize = false)
    private Incident incident;

    public void sync() {

    }

}
