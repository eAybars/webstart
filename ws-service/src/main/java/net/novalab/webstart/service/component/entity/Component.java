package net.novalab.webstart.service.component.entity;

import net.novalab.webstart.service.json.entity.JsonSerializable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;

/**
 * A component is a web start artifact, which can have sub components based on the hierarchy of their identifier URIs,
 * and provides information or downloadable resources about the item it represents. As an example an implementation of
 * the Component may be used to group other components like applications and documentations.
 */
public interface Component extends Artifact, Comparable<Component> {

    /**
     * Indicates the domain of the component and uniquely identifies a component. Must start with a "/" character
     * and end with a "/" character.
     *
     * @return URI identifying the component. May NOT be null
     */
    URI getIdentifier();

    /**
     * Resolves the given path to a URL. The given path is relative to the component identifier URI
     *
     * @param path a path, which is relative to the component identifier URI, to a resource related to this component
     * @return URL for the resource, or null if the resource could not be located
     */
    URL getResource(String path);

    @Override
    default int compareTo(Component o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    default JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (getDescription() != null) {
            builder.add("description", getDescription());
        }
        return builder
                .add("identifier", getIdentifier().toString())
                .add("title", getTitle())
                .add("icon", getIcon() == null ? "/icons/component.png" :
                        (getIcon().toString().charAt(0) == '/' ? getIcon().toString() :
                                getIdentifier().toString() + getIcon().toString()))
                .build();
    }
}
