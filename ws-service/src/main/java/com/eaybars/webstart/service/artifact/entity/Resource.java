package com.eaybars.webstart.service.artifact.entity;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Represents a downloadable resource like a document. Their identifier URI points to the downloadable item itself and
 * as a consequence, it should not end with a "/" character. Resource artifacts may not have sub components for that reason
 */
public class Resource extends Artifact {
    static {
        FieldValidation.install(Resource.class, "size", FieldValidator.NON_NULL.andThen(FieldValidator.INTEGRAL_VALUE));
    }

    public Resource(URI identifier) throws URISyntaxException {
        super(identifier);
        int extensionIndex = identifier.toString().lastIndexOf(".");
        int parentIndex = identifier.toString().lastIndexOf("/");
        if (extensionIndex <= parentIndex) {
            throw new URISyntaxException(identifier.toString(), "Resource identifier must end with an extension");
        }
    }

    /**
     * Size of the resource int bytes
     * @return number of bytes
     */
    public int getSize() {
        return toJson().getInt("size", 0);
    }

    /**
     * New size of the resource in bytes
     * @param size number of bytes
     */
    public void setSize(int size) {
        attributes().add("size", size).build();
    }
}
