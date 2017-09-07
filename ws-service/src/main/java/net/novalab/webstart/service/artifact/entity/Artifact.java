package net.novalab.webstart.service.artifact.entity;

import net.novalab.webstart.service.json.entity.JsonSerializable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

/**
 * Represents a web start artifact which provides information or downloadable resources about the item it represents.
 * This may be a grouping component, an executable application, some downloadable resources or any other stuff depending
 * on the subclass. An artifact may have sub artifacts inferred from the hierarchy of their identifier URIs.
 */
public interface Artifact extends Comparable<Artifact>, JsonSerializable {

    /**
     * Indicates the domain of the artifact and uniquely identifies it. Must start with a "/" character.
     *
     * @return URI identifying the component. May NOT be null
     */
    URI getIdentifier();

    /**
     * Title of the artifact. For example, this may be a title for an application, grouping component or a resource artifact
     *
     * @return title
     */
    String getTitle();

    /**
     * Provides an optional description for the artifact
     *
     * @return description
     */
    String getDescription();

    /**
     * Provides a URI to the icon representation for the artifact.
     *
     * @return icon representation of this artifact
     */
    URI getIcon();

    /**
     * Resolves a given URI in accordance to the identifier URI of this artifact and the supplied URI itself. The resolution
     * is performed by invoking getIdentifier().resolve(resource). More precisely, the URI resolution process is performed
     * according to the followings:
     * <ul>
     * <li>If the URI starts with a trailing / character, it is considered to be relative to the root of artifacts, which is {@code /}
     * So if the reference URI is {@code /icons/myIcon.png} than it is resolved as {@code /icons/myIcon.png}</li>
     * <li>If the URI does not starts with a trailing / character, it is considered to be relative to the identifier URI
     * of the artifact.</li>
     * <ul>
     * <li>If this artifact URI represents a domain, like {@code /my-component/} for instance, and the reference URI is
     * {@code icons/myIcon.png} than it is resolved as {@code /my-component/icons/myIcon.png}</li>
     * <li>If this artifact URI represents a resource like {@code /documents/tutorial.pdf}, than the given URI is considered to be relative
     * to the immediate parent fragment of identifier URI for this resource. In this case, if the reference URI is {@code icons/myIcon.png}
     * than it is resolved as {@code /documents/icons/myIcon.png}</li>
     * </ul>
     * </ul>
     *
     * @param uri uri to resolve against identifier URI of this artifact
     * @return resolved uri
     */
    default URI resolve(URI uri) {
        return getIdentifier().resolve(uri);
    }

    /**
     * Constructs a relative path string from the given URI if and only if the given URI is in the domain of this artifact.
     * The relative path string is used to locate resource URL through getResource method.
     *
     * @param uri
     * @return
     */
    default Optional<String> toRelativePath(URI uri) {
        URI relativize = getIdentifier().relativize(uri);
        return uri.equals(relativize) ? Optional.empty() : Optional.of(relativize.toString());
    }

    /**
     * Resolves the given path to a URL. The given path is relative to the component identifier URI
     *
     * @param path a path, which is relative to the component identifier URI, to a resource related to this component
     * @return URL for the resource, or null if the resource could not be located
     */
    URL getResource(String path);

    @Override
    default int compareTo(Artifact o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    @Override
    default JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("identifier", getIdentifier().toString())
                .add("title", getTitle());

        if (getIcon() != null) {
            builder.add("icon", resolve(getIcon()).toString());
        }
        if (getDescription() != null) {
            builder.add("description", getDescription());
        }
        return builder.build();
    }
}
