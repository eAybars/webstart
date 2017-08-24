package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedResource;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class PdfArtifactCreator extends AbstractArtifactCreator {
    @Override
    public Stream<? extends FileBasedArtifact> apply(File folder) {
        return Stream.of(folder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".pdf")))
                .sorted(Comparator.comparing(File::getName))
                .map(pdf -> new FileBasedResource(toIdentifier(pdf), pdf));
    }
}
