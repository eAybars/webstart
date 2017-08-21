package net.novalab.webstart.service.component.control;

import net.novalab.webstart.service.component.entity.Component;

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
public class Components {

    @Inject
    @Any
    Instance<ComponentSupplier> componentSuppliers;


    public Stream<Component> stream() {
        return StreamSupport.stream(componentSuppliers.spliterator(), false)
                .flatMap(ComponentSupplier::get);
    }

    public Hierarchy hierarchy() {
        return new Hierarchy(stream());
    }

    public Hierarchy hierarchy(Predicate<? super Component> filter) {
        return new Hierarchy(stream().filter(filter));
    }

    public static class Hierarchy {
        private Stream<Component> componentStream;

        private Hierarchy(Stream<Component> componentStream) {
            this.componentStream = componentStream;
        }

        public Stream<Component> top() {
            AtomicReference<Component> currentTop = new AtomicReference<>();
            return componentStream.sequential()
                    .sorted(Comparator.naturalOrder())
                    .map(c -> currentTop.accumulateAndGet(c, (oldC, newC) -> oldC == null ? newC :
                            (newC.getIdentifier().toString().startsWith(oldC.getIdentifier().toString()) ? oldC : newC)))
                    .distinct();
        }

        public Optional<Component> parent(URI identifier) {
            return componentStream
                    .filter(c -> identifier.toString().startsWith(c.getIdentifier().toString()))
                    .filter(c -> !identifier.equals(c.getIdentifier()))
                    .sorted(Comparator.reverseOrder())
                    .findFirst();
        }

        public Stream<Component> children(URI identifier) {
            return new Hierarchy(componentStream
                    .filter(c -> c.getIdentifier().toString().startsWith(identifier.toString()))
                    .filter(c -> !identifier.equals(c.getIdentifier()))
            ).top();
        }
    }
}
