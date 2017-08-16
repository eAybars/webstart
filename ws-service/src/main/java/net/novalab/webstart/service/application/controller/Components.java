package net.novalab.webstart.service.application.controller;

import net.novalab.webstart.service.application.entity.Component;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class Components {

    @Inject
    @Any
    Instance<ComponentSupplier> componentSuppliers;

    @Inject
    @Any
    Instance<Predicate<Component>> componentFilters;

    public Stream<Component> all() {
        return StreamSupport.stream(componentSuppliers.spliterator(), false)
                .flatMap(ComponentSupplier::get);
    }

    public Stream<Component> filtered() {
        return all().filter(this::test);
    }

    public boolean test(Component c) {
        return StreamSupport.stream(componentFilters.spliterator(), false)
                .allMatch(filter -> filter.test(c));
    }

}
