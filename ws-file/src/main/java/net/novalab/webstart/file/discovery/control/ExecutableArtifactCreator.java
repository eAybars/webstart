package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedExecutable;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.stream.Stream;

@ApplicationScoped
public class ExecutableArtifactCreator implements ArtifactCreator {

    private static final String EXECUTABLE_FILE_EXTENSION = ".jnlp";

    @Override
    public FileBasedArtifact apply(URI identifier, File folder) {
        return Stream.of(folder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(EXECUTABLE_FILE_EXTENSION)))
                .sorted(Comparator.comparing(File::getName))
                .findFirst()
                .map(exe -> new FileBasedExecutable(identifier, folder, exe))
                .orElse(null);
    }
}
