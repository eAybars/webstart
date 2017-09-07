package net.novalab.webstart.google.discovery.control;

import net.novalab.webstart.google.artifact.entity.CloudStorageArtifact;

import java.io.File;
import java.net.URI;
import java.util.function.Function;
import java.util.stream.Stream;

public interface ArtifactCreator extends Function<URI, Stream<? extends CloudStorageArtifact>> {
}
