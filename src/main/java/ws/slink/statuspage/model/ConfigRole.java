package ws.slink.statuspage.model;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.roles.ProjectRole;
import javax.validation.constraints.NotNull;

public class ConfigRole {

    private long          id;
    private String      name;
    private boolean selected;

    public ConfigRole(@NotNull ProjectRole role) {
        this.id       = role.getId();
        this.name     = role.getName();
        this.selected = false;
    }
    public ConfigRole(@NotNull ProjectRole role, boolean selected) {
        this.id       = role.getId();
        this.name     = role.getName();
        this.selected = selected;
    }

    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public boolean getSelected() {
        return selected;
    }

}
