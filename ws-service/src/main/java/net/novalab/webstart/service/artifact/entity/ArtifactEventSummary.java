package net.novalab.webstart.service.artifact.entity;

import net.novalab.webstart.service.json.entity.JsonSerializable;

import javax.json.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ArtifactEventSummary implements JsonSerializable {

    private Collection<? extends Artifact> unloadedArtifacts;
    private Collection<? extends Artifact> loadedArtifacts;
    private Collection<? extends Artifact> updatedArtifacts;

    public ArtifactEventSummary() {
        unloadedArtifacts = Collections.emptyList();
        loadedArtifacts = Collections.emptyList();
        updatedArtifacts = Collections.emptyList();
    }

    public ArtifactEventSummary merge(ArtifactEventSummary other) {
        ArtifactEventSummary merged = new ArtifactEventSummary();

        Set<Artifact> unloaded = new HashSet<>(getUnloadedArtifacts());
        unloaded.addAll(other.getUnloadedArtifacts());
        merged.setUnloadedArtifacts(unloaded);

        Set<Artifact> loaded = new HashSet<>(getLoadedArtifacts());
        loaded.addAll(other.getLoadedArtifacts());
        merged.setLoadedArtifacts(loaded);

        Set<Artifact> updated = new HashSet<>(getUpdatedArtifacts());
        updated.addAll(other.getUpdatedArtifacts());
        merged.setUpdatedArtifacts(updated);

        return merged;
    }

    public Collection<? extends Artifact> getUnloadedArtifacts() {
        return unloadedArtifacts;
    }

    public Collection<? extends Artifact> getLoadedArtifacts() {
        return loadedArtifacts;
    }

    public Collection<? extends Artifact> getUpdatedArtifacts() {
        return updatedArtifacts;
    }

    public void setUnloadedArtifacts(Collection<? extends Artifact> unloadedArtifacts) {
        this.unloadedArtifacts = unloadedArtifacts;
    }

    public void setLoadedArtifacts(Collection<? extends Artifact> loadedArtifacts) {
        this.loadedArtifacts = loadedArtifacts;
    }

    public void setUpdatedArtifacts(Collection<? extends Artifact> updatedArtifacts) {
        this.updatedArtifacts = updatedArtifacts;
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
