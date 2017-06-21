package net.novalab.webstart.service.application.entity;

import java.net.URI;
import java.util.Objects;

/**
 * Created by ertunc on 30/05/17.
 */
public abstract class AbstractComponent implements Component {
    private URI identifier;
    private String title;
    private String description;
    private URI iconUrl;

    public AbstractComponent(URI identifier) {
        this.identifier = Objects.requireNonNull(identifier);
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
        return iconUrl;
    }

    public void setIconUrl(URI iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractComponent that = (AbstractComponent) o;

        return getIdentifier().equals(that.getIdentifier());
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
    }
}
