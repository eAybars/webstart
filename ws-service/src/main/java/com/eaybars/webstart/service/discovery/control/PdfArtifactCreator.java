package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Resource;
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
public class PdfArtifactCreator implements ArtifactCreator {
    private static final Logger LOGGER = Logger.getLogger(PdfArtifactCreator.class.getName());

    @Override
    public Stream<Artifact> apply(Backend backend, List<URI> contents) {
        return contents.stream()
                .filter(((Predicate<URI>) backend::isDirectory).negate())
                .filter(c -> c.getPath().endsWith(".pdf"))
                .map(this::createResource)
                .filter(Objects::nonNull);
    }

    private Artifact createResource(URI rId) {
        try {
            return new Resource(rId);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Failed to create Resource artifact for " + rId, e);
            return null;
        }
    }

}
