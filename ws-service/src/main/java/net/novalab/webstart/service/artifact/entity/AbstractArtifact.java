package net.novalab.webstart.service.artifact.entity;

import java.net.URI;
import java.util.Objects;

/**
 * AbstractArtifact is a basis implementation for the artifacts. It provides basic functionality like descriptive
 * information, toString, equals and hashCode implementations and leaves the mapping of paths to URL to its subclass implementations.
 */
public abstract class AbstractArtifact implements Artifact {

    private URI identifier;
    private String title;
    private String description;
    private URI icon;

    public AbstractArtifact(URI identifier) {
        this.identifier = Objects.requireNonNull(identifier);
        String title = identifier.toString();
        title = title.substring(title.lastIndexOf('/'));
        int dotIndex = title.lastIndexOf('.');
        if (dotIndex >= 0) {
            title = title.substring(0, dotIndex);
        }
        this.title = title;
    }

    @Override
    public URI getIdentifier() {
        return identifier;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractArtifact that = (AbstractArtifact) o;

        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }

    @Override
    public String toString() {
        return getIdentifier().toString();
    }
}
