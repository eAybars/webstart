package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Component;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backends;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.stream.Stream;

/**
 * Creates a component from the resource/ URI for each backend. This component is invisible and can be used
 * to deploy some common files like images.
 */
@ApplicationScoped
public class ResourcesArtifactDiscovery implements ArtifactDiscovery {
    public static final URI RESOURCES_URI = URI.create("resources/");

    @Override
    public Stream<? extends Artifact> apply(Backends.BackendURI backendURI) {
        if (RESOURCES_URI.equals(backendURI.getUri())) {
            return Stream.of(backendURI.getBackend().createArtifact(Component.class, backendURI.getUri()));
        }
        return Stream.empty();
    }
}
