package net.novalab.webstart.google.discovery.control;

import com.google.cloud.storage.Blob;
import net.novalab.webstart.google.artifact.entity.CloudStorageArtifact;
import net.novalab.webstart.google.artifact.entity.CloudStorageComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ArtifactScanner extends AbstractArtifactCreator {
    private static final URI ROOT = URI.create("/");

    @Inject
    @Any
    Instance<ArtifactCreator> artifactCreators;

    public Stream<URI> rootURIs() {
        return blobs(ROOT)
                .filter(Blob::isDirectory)
                .map(Blob::getName)
                .map("/"::concat)
                .map(URI::create);
    }

    @Override
    public Stream<? extends CloudStorageArtifact> apply(URI path) {
        List<CloudStorageArtifact> artifacts = new LinkedList<>();

        StreamSupport.stream(artifactCreators.spliterator(), false)
                .filter(((Predicate<ArtifactCreator>)ArtifactScanner.class::isInstance).negate())
                .flatMap(cc -> cc.apply(path))
                .filter(Objects::nonNull)
                .forEach(artifacts::add);

        boolean addedComponent = !artifacts.isEmpty();

        List<Blob> blobs = blobs(path).collect(Collectors.toList());

        blobs.stream()
                .filter(Blob::isDirectory)
                .map(Blob::getName)
                .map("/"::concat)
                .map(URI::create)
                .flatMap(this::apply)
                .forEach(artifacts::add);


        if (!addedComponent && !artifacts.isEmpty() && !ROOT.equals(path)) {
            CloudStorageComponent component = new CloudStorageComponent(path);
            blobs.stream()
                    .filter(((Predicate<Blob>)Blob::isDirectory).negate())
                    .map(Blob::getName)
                    .map("/"::concat)
                    .forEach(component.getResources()::add);
            artifacts.add(0, component);
        }

        return artifacts.stream();
    }
}
