package com.eaybars.webstart.service.artifact.entity;

import com.eaybars.webstart.service.uri.control.URIBuilder;

import javax.json.JsonNumber;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

/**
 * Represents an executable web start artifact, like applications or installers. It may contain sub
 * artifacts and may belong to a parent artifact or might be a top level artifact depending of its identifier URI
 */
public class Executable extends Artifact {
    static {
        FieldValidation.install(Executable.class, new HashMap<String, FieldValidator>() {{
            put("executable", FieldValidator.READ_ONLY);
            put("dateModified", FieldValidator.INTEGRAL_VALUE);
        }});
    }

    private URI executable;

    public Executable(URI executable) throws URISyntaxException {
        super(URIBuilder.from(executable).addParentPathFromSource().addPath("/").build());
        if (!executable.getPath().endsWith(".jnlp")) {
            throw new URISyntaxException(executable.toString(), "Executable URI must end with \".jnlp\"");
        }
        this.executable = executable;
        //ensure executable attribute is set in Json
        setAttributes(baseAttributes().build());
    }

    @Override
    protected JsonObjectBuilder baseAttributes() {
        return executable == null ? super.baseAttributes() :
                super.baseAttributes().add("executable", executable.toString());
    }

    /**
     * Provides release version information for this executable application
     *
     * @return version, may be null
     */
    public String getVersion() {
        return toJson().getString("version", null);
    }

    /**
     * Set new release version information for this executable application
     *
     * @param version new version
     */
    public void setVersion(String version) {
        if (version == null) {
            attributes().addNull("version").build();
        } else {
            attributes().add("version", version).build();
        }
    }

    /**
     * Provides information about the release date of the application.
     *
     * @return latest release date, may not be null
     */
    public Date getDateModified() {
        return JsonValue.NULL.equals(toJson().get("dateModified")) ? null :
                Optional.ofNullable(toJson().getJsonNumber("dateModified"))
                        .map(JsonNumber::longValue)
                        .map(Date::new)
                        .orElse(null);
    }

    /**
     * Set new Date modified information
     *
     * @param dateModified new date modified
     */
    public void setDateModified(Date dateModified) {
        if (dateModified == null) {
            attributes().addNull("dateModified").build();
        } else {
            attributes().add("dateModified", dateModified.getTime()).build();
        }
    }

    /**
     * path to the executable JNLP file
     *
     * @return URI is interpreted as described in the artifact documentation
     */
    public URI getExecutable() {
        return executable;
    }

}
