package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;

import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface ArtifactCreator extends Function<File, List<? extends FileBasedArtifact>> {
}
