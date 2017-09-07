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
        if ("".equals(title) || title.charAt(0) != '/') {
            throw new IllegalArgumentException("Identifier URI must start with a / character");
        }

        if (title.charAt(title.length() - 1) == '/') {
            int index = title.lastIndexOf('/', title.length() - 2);
            title = title.substring(index + 1, title.length() - 1);
        } else {
            int startIndex = title.lastIndexOf('/');
            int endIndex = title.lastIndexOf('.');
            if (endIndex < startIndex) {
                endIndex = title.length();
            }
            title = title.substring(startIndex + 1, endIndex);
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
