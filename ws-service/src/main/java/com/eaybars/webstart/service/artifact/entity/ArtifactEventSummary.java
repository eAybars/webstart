package com.eaybars.webstart.service.artifact.entity;

import com.eaybars.webstart.service.json.entity.JsonSerializable;

import javax.json.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public class ArtifactEventSummary implements JsonSerializable {

    public static final ArtifactEventSummary EMPTY = new ArtifactEventSummary(emptySet(), emptySet(), emptySet());

    private Set<Artifact> unloadedArtifacts;
    private Set<Artifact> loadedArtifacts;
    private Set<Artifact> updatedArtifacts;

    public ArtifactEventSummary() {
        this(new HashSet<>(), new HashSet<>(), new HashSet<>());
    }

    private ArtifactEventSummary(Set<Artifact> unloadedArtifacts,
                                 Set<Artifact> loadedArtifacts,
                                 Set<Artifact> updatedArtifacts) {
        this.unloadedArtifacts = unloadedArtifacts;
        this.loadedArtifacts = loadedArtifacts;
        this.updatedArtifacts = updatedArtifacts;
    }

    public static ArtifactEventSummary loadOnly() {
        return new ArtifactEventSummary(emptySet(), new HashSet<>(), emptySet());
    }

    public static ArtifactEventSummary unloadOnly() {
        return new ArtifactEventSummary(new HashSet<>(), emptySet(), emptySet());
    }

    public ArtifactEventSummary merge(ArtifactEventSummary other) {
        ArtifactEventSummary merged = new ArtifactEventSummary();

        merged.getUnloadedArtifacts().addAll(getUnloadedArtifacts());
        merged.getUnloadedArtifacts().addAll(other.getUnloadedArtifacts());

        merged.getLoadedArtifacts().addAll(getLoadedArtifacts());
        merged.getLoadedArtifacts().addAll(other.getLoadedArtifacts());

        merged.getUpdatedArtifacts().addAll(getUpdatedArtifacts());
        merged.getUpdatedArtifacts().addAll(other.getUpdatedArtifacts());

        return merged;
    }

    public Set<Artifact> getUnloadedArtifacts() {
        return unloadedArtifacts;
    }

    public Set<Artifact> getLoadedArtifacts() {
        return loadedArtifacts;
    }

    public Set<Artifact> getUpdatedArtifacts() {
        return updatedArtifacts;
    }


    @Override
    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (!unloadedArtifacts.isEmpty()) {
            builder.add("unloaded", toArray(unloadedArtifacts));
        }
        if (!loadedArtifacts.isEmpty()) {
            builder.add("loaded", toArray(loadedArtifacts));
        }
        if (!updatedArtifacts.isEmpty()) {
            builder.add("updated", toArray(updatedArtifacts));
        }
        return builder.build();
    }

    private JsonArray toArray(Collection<? extends Artifact> artifacts) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        artifacts.stream()
                .map(Artifact::toJson)
                .forEach(arrayBuilder::add);
        return arrayBuilder.build();
    }
}
