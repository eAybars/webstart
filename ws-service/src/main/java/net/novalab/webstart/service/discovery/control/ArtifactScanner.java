package net.novalab.webstart.service.discovery.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.Component;
import net.novalab.webstart.service.backend.control.Backends;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class ArtifactScanner implements ArtifactDiscovery {

    @Inject
    @Any
    Instance<ArtifactDiscovery> discoveries;

    @Override
    public Stream<? extends Artifact> apply(Backends.BackendURI backendURI) {
        List<Artifact> artifacts = new LinkedList<>();

        StreamSupport.stream(discoveries.spliterator(), false)
                .filter(((Predicate<ArtifactDiscovery>)ArtifactScanner.class::isInstance).negate())
                .flatMap(ad -> ad.apply(backendURI))
                .filter(Objects::nonNull)
                .forEach(artifacts::add);

        boolean addedComponent = !artifacts.isEmpty();

        backendURI.getBackend().contents(backendURI.getUri())
                .filter(backendURI.getBackend()::isDirectory)
                .map(backendURI::newBackendURI)
                .flatMap(this::apply)
                .forEach(artifacts::add);

        if (!addedComponent && !artifacts.isEmpty() && !"".equals(backendURI.getUri().toString())) {
            artifacts.add(0, backendURI.getBackend().createArtifact(Component.class, backendURI.getUri()));
        }

        return artifacts.stream();
    }

}
