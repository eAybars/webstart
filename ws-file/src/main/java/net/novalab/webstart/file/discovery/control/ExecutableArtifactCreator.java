package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedExecutable;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class ExecutableArtifactCreator extends AbstractArtifactCreator {

    private static final String EXECUTABLE_FILE_EXTENSION = ".jnlp";

    @Override
    public List<? extends FileBasedArtifact> apply(File folder) {
        return Stream.of(folder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(EXECUTABLE_FILE_EXTENSION)))
                .sorted(Comparator.comparing(File::getName))
                .findFirst()
                .map(exe -> new FileBasedExecutable(toIdentifier(folder), folder, exe))
                .map(Collections::singletonList)
                .orElse(Collections.emptyList());
    }
}
