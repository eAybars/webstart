package net.novalab.webstart.service.artifact.entity;

import net.novalab.webstart.service.json.control.Enrich;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.net.URI;
import java.util.Date;
import java.util.Map;

/**
 * Represents an executable web start component, like applications or installers. Being a component, it may contain sub
 * components and may belong to a parent component or might be a top level component depending of its identifier URI
 */
public interface Executable extends Component {

    /**
     * Attributes for the executable application. May contain some extra information like build number,
     * release notes and so on
     *
     * @return attributes
     */
    Map<String, Object> getAttributes();

    /**
     * Provides release version information for this executable application
     *
     * @return version, may be null
     */
    String getVersion();

    /**
     * Provides information about the release date of the application.
     *
     * @return latest release date, may not be null
     */
    Date getDateModified();

    /**
     * URI is interpreted as described in the artifact documentation
     *
     * @return path to the executable JNLP file
     */
    URI getExecutable();

    @Override
    default JsonObject toJson() {
        JsonObjectBuilder attributes = Json.createObjectBuilder();
        getAttributes().forEach((k, v) -> attributes.add(k, v.toString()));

        JsonObjectBuilder objectBuilder = Enrich.object(Component.super.toJson());
        if (getVersion() != null) {
            objectBuilder.add("version", getVersion());
        }

        return objectBuilder
                .add("type", "executable")
                .add("executable", resolve(getExecutable()).toString())
                .add("dateModified", getDateModified().getTime())
                .add("attributes", attributes)
                .build();
    }
}
