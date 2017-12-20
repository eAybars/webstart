package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backend;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ArtifactDiscovery {

    private static Logger LOGGER = Logger.getLogger(ArtifactDiscovery.class.getName());

    @Inject
    @Any
    Instance<ArtifactCreator> discoveries;

    @Inject
    Backend backend;

    public Stream<Artifact> apply(URI uri) {
        if (!backend.isDirectory(uri)) {
            throw new IllegalArgumentException(uri + " is not a directory");
        }

        List<Artifact> artifacts = new LinkedList<>();

        List<URI> contents = backend.contents(uri).collect(Collectors.toList());

        StreamSupport.stream(discoveries.spliterator(), false)
                .flatMap(ad -> ad.apply(backend, contents))
                .filter(Objects::nonNull)
                .forEach(artifacts::add);

        boolean directoryArtifactAdded = artifacts.stream().map(Artifact::getIdentifier).anyMatch(uri::equals);

        contents.stream()
                .filter(backend::isDirectory)
                .flatMap(this::apply)
                .forEach(artifacts::add);

        if (!directoryArtifactAdded && !artifacts.isEmpty() && !Backend.ROOT.equals(uri)) {
            try {
                artifacts.add(0, new Artifact(uri));
            } catch (URISyntaxException e) {
                LOGGER.log(Level.SEVERE, "Error creating plain artifact for " + uri, e);
            }
        }

        return artifacts.stream();
    }

}
