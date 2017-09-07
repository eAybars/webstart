package net.novalab.webstart.service.artifact.entity;

import net.novalab.webstart.service.json.control.Enrich;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents an artifact which may have sub components. Component identifier URI represents a domain and therefore
 * must end with a "/" character. Components are used to group artifacts.
 */
public interface Component extends Artifact {

    @Override
    default JsonObject toJson() {
        JsonObjectBuilder objectBuilder = Enrich.object(Artifact.super.toJson());
        return objectBuilder.add("type", "component").build();
    }
}
