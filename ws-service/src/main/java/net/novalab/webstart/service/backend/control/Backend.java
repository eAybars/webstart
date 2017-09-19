package net.novalab.webstart.service.backend.control;

import net.novalab.webstart.service.artifact.entity.Artifact;

import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

public interface Backend {

    URI getName();

    Stream<URI> contents(URI parent);

    default boolean isDirectory(URI uri) {
        return uri.toString().endsWith("/");
    }

    <T extends Artifact> T createArtifact(Class<T> type, URI target);

    Optional<Storage> getStorage();
}
