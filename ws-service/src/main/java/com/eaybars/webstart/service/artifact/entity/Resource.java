package com.eaybars.webstart.service.artifact.entity;

import com.eaybars.webstart.service.json.control.Enrich;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents a downloadable resource like a document. Their identifier URI points to the downloadable item itself and
 * as a consequence, it should not end with a "/" character. Resource artifacts may not have sub components for that reason
 */
public interface Resource extends Artifact {

    @Override
    default JsonObject toJson() {
        JsonObjectBuilder objectBuilder = Enrich.object(Artifact.super.toJson());
        return objectBuilder.add("type", "resource").build();

    }
}
