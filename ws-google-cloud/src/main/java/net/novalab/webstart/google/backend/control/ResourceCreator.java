package net.novalab.webstart.google.backend.control;

import net.novalab.webstart.google.artifact.entity.GCSResource;
import net.novalab.webstart.service.artifact.entity.Artifact;

import java.net.URI;

public class ResourceCreator extends ArtifactCreator {

    public ResourceCreator(GCSBackend backend) {
        super(backend);
    }

    @Override
    public Artifact apply(URI uri) {
        return new GCSResource(uri);
    }
}
