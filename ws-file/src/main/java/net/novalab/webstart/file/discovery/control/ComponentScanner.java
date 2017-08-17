package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.component.entity.FileBasedComponent;
import net.novalab.webstart.file.component.entity.FileBasedExecutable;
import net.novalab.webstart.file.discovery.entity.ArtifactRoot;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by ertunc on 20/06/17.
 */
@ApplicationScoped
public class ComponentScanner implements Function<File, List<? extends FileBasedComponent>> {

    @Inject
    @ArtifactRoot
    private File artifactRoot;

    @Inject
    @Any
    Instance<ComponentCreator> componentCreators;

    @Override
    public List<? extends FileBasedComponent> apply(File folder) {
        List<FileBasedComponent> components = new LinkedList<>();

        StreamSupport.stream(componentCreators.spliterator(), false)
                .map(cc -> cc.apply(toComponentIdentifier(folder), folder))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(components::add);

        boolean addedComponent = !components.isEmpty();

        Stream.of(folder.listFiles(File::isDirectory))
                .map(this::apply)
                .forEach(components::addAll);

        if (!addedComponent && !components.isEmpty()) {
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
