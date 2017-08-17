package net.novalab.webstart.service.component.entity;

import java.net.URI;

/**
 * A sufficient implementation of the Artifact interface. It may be used as the basis for other components
 */
public class SimpleArtifact implements Artifact {

    private static final long serialVersionUID = -7276762405139744280L;

    private String title;
    private String description;
    private URI icon;

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public URI getIcon() {
        return icon;
    }

    public void setIcon(URI icon) {
        this.icon = icon;
    }
}
