package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by ertunc on 20/06/17.
 */
@ApplicationScoped
public class ArtifactScanner extends AbstractArtifactCreator {

    @Inject
    @Any
    Instance<ArtifactCreator> componentCreators;

    @Override
    public Stream<? extends FileBasedArtifact> apply(File folder) {
        List<FileBasedArtifact> components = new LinkedList<>();

        StreamSupport.stream(componentCreators.spliterator(), false)
                .filter(((Predicate<ArtifactCreator>)ArtifactScanner.class::isInstance).negate())
                .flatMap(cc -> cc.apply(folder))
                .filter(Objects::nonNull)
                .forEach(components::add);

        boolean addedComponent = !components.isEmpty();

        Stream.of(folder.listFiles(File::isDirectory))
                .flatMap(this::apply)
                .forEach(components::add);

        if (!addedComponent && !components.isEmpty()) {
            components.add(0, new FileBasedComponent(toIdentifier(folder), folder));
        }

        return components.stream();
    }

}
