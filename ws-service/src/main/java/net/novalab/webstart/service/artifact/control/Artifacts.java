package net.novalab.webstart.service.artifact.control;

import net.novalab.webstart.service.artifact.entity.Artifact;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class Artifacts {

    @Inject
    @Any
    Instance<ArtifactSupplier> componentSuppliers;


    public Stream<Artifact> stream() {
        return StreamSupport.stream(componentSuppliers.spliterator(), false)
                .flatMap(ArtifactSupplier::get);
    }

    public Hierarchy hierarchy() {
        return new Hierarchy(stream());
    }

    public Hierarchy hierarchy(Predicate<? super Artifact> filter) {
        return new Hierarchy(stream().filter(filter));
    }

    public static class Hierarchy {
        private Stream<Artifact> componentStream;

        private Hierarchy(Stream<Artifact> componentStream) {
            this.componentStream = componentStream;
        }

        public Stream<Artifact> top() {
            AtomicReference<Artifact> currentTop = new AtomicReference<>();
            return componentStream.sequential()
                    .sorted(Comparator.naturalOrder())
                    .map(c -> currentTop.accumulateAndGet(c, (oldC, newC) -> oldC == null ? newC :
                            (newC.getIdentifier().toString().startsWith(oldC.getIdentifier().toString()) ? oldC : newC)))
                    .distinct();
        }

        public Optional<Artifact> parent(URI identifier) {
            return parents(identifier).findFirst();
        }

        public Stream<Artifact> parents(URI identifier) {
            return componentStream
                    .filter(c -> identifier.toString().startsWith(c.getIdentifier().toString()))
                    .filter(c -> !identifier.equals(c.getIdentifier()))
                    .sorted(Comparator.reverseOrder());
        }

        public Stream<Artifact> children(URI identifier) {
            return new Hierarchy(componentStream
                    .filter(c -> c.getIdentifier().toString().startsWith(identifier.toString()))
                    .filter(c -> !identifier.equals(c.getIdentifier()))
            ).top();
        }
    }
}
