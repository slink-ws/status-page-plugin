package ws.slink.statuspage.model;

import com.google.gson.annotations.Expose;
import ws.slink.statuspage.service.StatuspageService;
import ws.slink.statuspage.tools.JiraTools;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class IssueIncident {

    private String projectKey;
    private String pageId;
    private String incidentId;
    private String createdBy;
    private String linkedBy;
    private LocalDateTime createdAt;
    private LocalDateTime linkedAt;

    @Expose(serialize = false, deserialize = false)
    private AtomicReference<Incident> incident = new AtomicReference<>(null);

    @Expose(serialize = false, deserialize = false)
    private AtomicBoolean synced = new AtomicBoolean(false);

    @Expose(serialize = false, deserialize = false)
    private String pageName = null;
    @Expose(serialize = false, deserialize = false)
    private String incidentName = null;

    public void sync() {
        StatuspageService.instance().get(projectKey).ifPresent(statusPage -> {
//            System.out.println("---> statuspage: " + statusPage);
            statusPage.getPage(pageId, false).ifPresent(page -> {
                pageName = page.name();
//                System.out.println("---> page: " + page);
                statusPage.getIncident(pageId, incidentId, false).ifPresent(inc -> {
                    incidentName = inc.name();
//                    System.out.println("---> incident: " + inc);
                });
            });
        });
//            sp.getIncident(pageId, incidentId, true).ifPresent(i -> {
//            System.out.println("---> synced incident: " + i);
//            incident.set(i);
//            synced.set(true);
//        }));
    }

    public String pageId() {
        return pageId;
    }
    public IssueIncident pageId(String value) {
        this.pageId = value;
        return this;
    }
    public String incidentId() {
        return incidentId;
    }
    public IssueIncident incidentId(String value) {
        this.incidentId = value;
        return this;
    }
    public String createdBy() {
        return createdBy;
    }
    public IssueIncident createdBy(String value) {
        this.createdBy = value;
        return this;
    }
    public String linkedBy() {
        return linkedBy;
    }
    public IssueIncident linkedBy(String value) {
        this.linkedBy = value;
        return this;
    }
    public LocalDateTime createdAt() {
        return createdAt;
    }
    public IssueIncident createdAt(LocalDateTime value) {
        this.createdAt = value;
        return this;
    }
    public LocalDateTime linkedAt() {
        return linkedAt;
    }
    public IssueIncident linkedAt(LocalDateTime value) {
        this.linkedAt = value;
        return this;
    }
    public String projectKey() {
        return projectKey;
    }
    public IssueIncident projectKey(String  value) {
        this.projectKey = value;
        return this;
    }


    public String toJsonString() {
        return JiraTools.getGsonObject().toJson(this);
    }
    public String toString() {
        if (!synced.get()) {
            sync();
        }
//        if (null != incident)
//            return incident.get().page().name() + " : " + incident.get().name() + " : " + incident.get().status().value();
//        else
        if (null != pageName && null != incidentName)
            return projectKey + " : " + pageName + " : " + incidentName;
        else
            return projectKey + " : " + pageId + " : " + incidentId;
    }
}
