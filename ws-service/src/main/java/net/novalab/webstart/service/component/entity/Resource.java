package net.novalab.webstart.service.component.entity;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.net.URI;
import java.util.List;

/**
 * Represents a collection of downloadable resources, such as documentation, helper files and so on.
 */
public interface Resource extends Component {

    /**
     * Represents a single downloadable resource within a Resource component
     */
    interface Artifact extends net.novalab.webstart.service.component.entity.Artifact {
        URI getURI();
        Resource getOwner();

        @Override
        default JsonObject toJson() {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            if (getDescription() != null) {
                builder.add("description", getDescription());
            }
            return builder
                    .add("title", getTitle())
                    .add("icon", getIcon() == null ? "/icons/artifact.png" :
                            (getIcon().toString().charAt(0) == '/' ? getIcon().toString() :
                                    getOwner().getIdentifier().toString() + getIcon().toString()))
                    .build();
        }
    }

    /**
     * Provides list of downloadable artifacts contained within this resource component
     * @return list of downloadable artifacts
     */
    List<? extends Resource.Artifact> getArtifacts();
}
