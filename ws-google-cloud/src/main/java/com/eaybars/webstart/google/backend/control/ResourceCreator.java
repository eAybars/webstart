package com.eaybars.webstart.google.backend.control;

import com.eaybars.webstart.google.artifact.entity.GCSResource;
import com.eaybars.webstart.service.artifact.entity.Artifact;

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
