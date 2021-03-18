package ws.slink.statuspage.model;

import com.atlassian.jira.project.Project;
import javax.validation.constraints.NotNull;

public class ConfigProject {

    private String       key;
    private String      name;
    private boolean selected;

    public ConfigProject(@NotNull Project project) {
        this.key      = project.getKey();
        this.name     = project.getName();
        this.selected = false;
    }
    public ConfigProject(@NotNull Project project, boolean selected) {
        this.key      = project.getKey();
        this.name     = project.getName();
        this.selected = selected;
    }

    public String getKey() {
        return key;
    }
    public String getName() {
        return name;
    }
    public boolean getSelected() {
        return selected;
    }

}
