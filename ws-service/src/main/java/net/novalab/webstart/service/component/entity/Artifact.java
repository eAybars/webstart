package net.novalab.webstart.service.component.entity;

import net.novalab.webstart.service.json.entity.JsonSerializable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.Serializable;
import java.net.URI;

/**
 * Represents a web start artifact. This may be a grouping component, an application, some
 * downloadable resources or any other stuff depending on the subclass. Artifact forms the basis to all these
 * subclasses, and therefore it is not meaningful on its own.
 */
public interface Artifact extends Serializable, JsonSerializable {

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
     * Provides a URI to the icon representation for the artifact. Interpretation of the URI depends on the followings:<br>
     * <li>If the URI starts with a trailing / character, it is considered to be relative to the root of artifacts. By default
     * [application-domain]/downloads (i.e. www.myapplication.com/downloads) is the root for all artifacts. So if the reference
     * URI is /icons/myIcon.png than it is interpreted as www.myapplication.com/downloads/icons/myIcon.png</li>
     * <li>If the URI does not starts with a trailing / character, it is considered to be relative to the identifier URI
     * of the component to which this artifact represents or belongs to. If this artifact belongs to a domain /my-component/
     * and the reference URI is icons/myIcon.png than it is interpreted as [application-domain]/downloads/my-component/icons/myIcon.png</li>
     *
     * @return
     */
    URI getIcon();

    /**
     * Provides a JSON representation for the artifact
     *
     * @return
     */
    @Override
    default JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (getDescription() != null) {
            builder.add("description", getDescription());
        }
        return builder
                .add("title", getTitle())
                .add("icon", getIcon() == null ? "/icons/artifact.png" : getIcon().toString())
                .build();
    }

}
