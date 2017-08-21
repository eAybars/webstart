package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;

import java.io.File;
import java.net.URI;
import java.util.function.BiFunction;

public interface ArtifactCreator extends BiFunction<URI, File, FileBasedArtifact> {
}
