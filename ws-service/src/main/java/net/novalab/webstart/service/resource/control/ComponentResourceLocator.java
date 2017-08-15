package net.novalab.webstart.service.resource.control;

import jnlp.sample.servlet.ResourceLocator;
import net.novalab.webstart.service.application.controller.Components;
import net.novalab.webstart.service.application.entity.Component;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URL;
import java.util.Comparator;
import java.util.Optional;

@ApplicationScoped
public class ComponentResourceLocator implements ResourceLocator {
    @Inject
    Components components;

    @Override
    public URL apply(String path) {
        return componentForResource(path)
                .map(c -> c.getResource(path.substring((c.getIdentifier().toString() + (c.getIdentifier().toString().endsWith("/") ? "" : "/")).length())))
                .orElse(null);
    }

    public Optional<Component> componentForResource(String path) {
        return components.all()
                .sorted(Comparator.reverseOrder())
                .filter(c -> path.startsWith(c.getIdentifier().toString() + (c.getIdentifier().toString().endsWith("/") ? "" : "/")))
                .findFirst()
                .map(c -> components.test(c) ? null : c);
    }
}
