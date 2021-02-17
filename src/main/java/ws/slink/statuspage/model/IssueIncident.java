package ws.slink.statuspage.model;

import org.apache.commons.lang3.StringUtils;
import ws.slink.statuspage.tools.JiraTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IssueIncident {

    private String projectKey;
    private String pageId;
    private String incidentId;
    private String createdBy;
    private String linkedBy;
    private LocalDateTime createdAt;
    private LocalDateTime linkedAt;

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
    public String createdAtStr() {
        if (null != createdAt)
//            return createdAt.format(DateTimeFormatter.ofPattern(JiraTools.instance().getDateTimeFormat()));
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")) + " UTC";
        else
            return "";
    }
    public IssueIncident createdAt(LocalDateTime value) {
        this.createdAt = value;
        return this;
    }
    public LocalDateTime linkedAt() {
        return linkedAt;
    }
    public String linkedAtStr() {
        if (null != linkedAt)
//            return linkedAt.format(DateTimeFormatter.ofPattern(JiraTools.getDateTimeFormat()));
            return linkedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")) + " UTC";
        else
            return "";
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

    public boolean isLinked() {
        return StringUtils.isNotBlank(this.linkedBy);
    }
    public boolean isCreated() {
        return StringUtils.isNotBlank(this.createdBy);
    }

    @Override
    public int hashCode() {
        return (projectKey + "#" + pageId + "#" + incidentId).hashCode();
    }
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (null == other)
            return false;
        if (this.getClass() != other.getClass())
            return false;
        IssueIncident ii = (IssueIncident)other;

        if (StringUtils.isBlank(this.projectKey) && !(StringUtils.isBlank(ii.projectKey)))
            return false;
        if (!this.projectKey.equals(ii.projectKey))
            return false;

        if (StringUtils.isBlank(this.pageId) && !(StringUtils.isBlank(ii.pageId)))
            return false;
        if (!this.pageId.equals(ii.pageId))
            return false;

        if (StringUtils.isBlank(this.incidentId) && !(StringUtils.isBlank(ii.incidentId)))
            return false;
        if (!this.incidentId.equals(ii.incidentId))
            return false;

        return true;
    }

    public String toJsonString() {
        try {
            return JiraTools.instance().getGsonObject().toJson(this);
        } catch (Exception e) {
            return "{}";
        }
    }
    public String toString() {
        return projectKey + " : " + pageId + " : " + incidentId;
    }
}
