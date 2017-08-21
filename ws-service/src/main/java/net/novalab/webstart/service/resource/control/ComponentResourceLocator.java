package net.novalab.webstart.service.resource.control;

import jnlp.sample.servlet.ResourceLocator;
import net.novalab.webstart.service.component.control.Components;
import net.novalab.webstart.service.component.entity.Component;
import net.novalab.webstart.service.filter.entity.AccessFilter;
import net.novalab.webstart.service.filter.entity.AggregatedFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

@ApplicationScoped
public class ComponentResourceLocator implements ResourceLocator {

    @Inject
    Components components;
    @Inject
    @AggregatedFilter
    @AccessFilter
    Predicate<Component> accessFilter;

    @Override
    public URL apply(String path) {
        return componentForResource(path)
                .map(c -> c.getResource(path.substring((c.getIdentifier().toString() + (c.getIdentifier().toString().endsWith("/") ? "" : "/")).length())))
                .orElse(null);
    }

    public Optional<Component> componentForResource(String path) {
        return components.stream()
                .sorted(Comparator.reverseOrder())
                .filter(c -> path.startsWith(c.getIdentifier().toString() + (c.getIdentifier().toString().endsWith("/") ? "" : "/")))
                .findFirst()
                .map(c -> accessFilter.test(c) ? c: null);
    }
}
