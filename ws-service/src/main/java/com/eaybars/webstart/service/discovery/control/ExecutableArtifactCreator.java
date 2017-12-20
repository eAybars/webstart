package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Executable;
import com.eaybars.webstart.service.backend.control.Backend;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
public class ExecutableArtifactCreator implements ArtifactCreator {
    private static final Logger LOGGER = Logger.getLogger(ExecutableArtifactCreator.class.getName());

    @Override
    public Stream<Artifact> apply(Backend backend, List<URI> contents) {
        return contents.stream()
                .filter(((Predicate<URI>) backend::isDirectory).negate())
                .filter(c -> c.getPath().endsWith(".jnlp"))
                .sorted()
                .map(this::createExecutable)
                .filter(Objects::nonNull)
                .limit(1);
    }

    private Artifact createExecutable(URI executableURI) {
        try {
            return new Executable(executableURI);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Failed to create Executable artifact for " + executableURI, e);
            return null;
        }
    }
}
