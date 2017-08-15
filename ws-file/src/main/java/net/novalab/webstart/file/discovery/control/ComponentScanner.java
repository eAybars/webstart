package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.discovery.entity.FileBasedComponent;
import net.novalab.webstart.file.discovery.entity.FileBasedExecutable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by ertunc on 20/06/17.
 */
@ApplicationScoped
public class ComponentScanner implements Function<File, List<? extends FileBasedComponent>> {

    @Inject
    @ArtifactRoot
    private File artifactRoot;

    @Override
    public List<? extends FileBasedComponent> apply(File folder) {
        List<FileBasedComponent> components = new LinkedList<>();
        Stream.of(folder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".jnlp")))
                .sorted(Comparator.comparing(File::getName))
                .findFirst()
                .map(exe -> new FileBasedExecutable(toComponentIdentifier(folder), folder, exe))
                .ifPresent(components::add);

        boolean addedExecutable = !components.isEmpty();
        Stream.of(folder.listFiles(File::isDirectory))
                .map(this::apply)
                .forEach(components::addAll);

        if (!addedExecutable && !components.isEmpty()) {
            components.add(0, new FileBasedComponent(toComponentIdentifier(folder), folder));
        }

        return components;
    }

    private URI toComponentIdentifier(File folder) {
        return URI.create("/"+artifactRoot.toURI().relativize(folder.toURI()));
    }

    public File getArtifactRoot() {
        return artifactRoot;
    }
}
