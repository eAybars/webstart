package net.novalab.webstart.service.component.entity;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Objects;

/**
 * AbstractComponent is a basis implementation for the components. It provides basic functionality like descriptive
 * information, toString, equals and hashCode implementations and leaves the mapping of paths to URL to its subclass implementations.
 */
public abstract class AbstractComponent extends SimpleArtifact implements Component {

    private static final long serialVersionUID = 4476751500551917113L;
    private URI identifier;

    public AbstractComponent(URI identifier) {
        this.identifier = Objects.requireNonNull(identifier);
    }

    @Override
    public URI getIdentifier() {
        return identifier;
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

    @Override
    public String toString() {
        return getIdentifier().toString();
    }

    @Override
    public JsonObject toJson() {
        return Component.super.toJson();
    }
}
