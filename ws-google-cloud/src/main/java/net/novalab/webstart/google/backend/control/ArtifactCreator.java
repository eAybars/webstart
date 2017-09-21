package net.novalab.webstart.google.backend.control;

import net.novalab.webstart.service.artifact.entity.Artifact;

import java.net.URI;
import java.util.function.Function;

public abstract class ArtifactCreator implements Function<URI, Artifact> {
    private GCSBackend backend;

    public ArtifactCreator(GCSBackend backend) {
        this.backend = backend;
    }

    public GCSBackend getBackend() {
        return backend;
    }

}
