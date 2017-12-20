package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backend;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Creates a component from the resource/ URI for each backend. This component is invisible and can be used
 * to deploy some common files like images.
 */
@ApplicationScoped
public class ResourcesArtifactCreator implements ArtifactCreator {
    private static final Logger LOGGER = Logger.getLogger(ResourcesArtifactCreator.class.getName());
    public static final URI RESOURCES_URI = URI.create("/resources/");

    @Override
    public Stream<Artifact> apply(Backend backend, List<URI> contet) {
        if (contet.contains(RESOURCES_URI)) {
            try {
                Artifact artifact = new Artifact(RESOURCES_URI);
                return Stream.of(artifact);
            } catch (URISyntaxException e) {
                LOGGER.log(Level.SEVERE, "Failed to create resources artifact", e);
            }
        }
        return Stream.empty();
    }
}
