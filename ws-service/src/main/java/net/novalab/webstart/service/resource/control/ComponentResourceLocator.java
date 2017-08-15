package net.novalab.webstart.service.resource.control;

import jnlp.sample.servlet.ResourceLocator;
import net.novalab.webstart.service.application.controller.Components;
import net.novalab.webstart.service.application.entity.Component;
import net.novalab.webstart.service.application.entity.Executable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.Serializable;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SessionScoped
public class ComponentResourceLocator implements ResourceLocator, Serializable {
    @Inject
    Components components;

    private Set<Component> modifiedExecutables;

    public ComponentResourceLocator() {
        modifiedExecutables = new HashSet<>();
    }

    public void onExecutableModification(@Observes(notifyObserver = Reception.IF_EXISTS) Executable e) {
        if (components.filtered().anyMatch(e::equals)) {
            modifiedExecutables.add(e);
        }
    }

    @Override
    public URL apply(String path) {
        Optional<Component> optional = componentForResource(path);
        if (path.endsWith(".jnlp")){
            optional.ifPresent(modifiedExecutables::remove);
        }
        return optional
                .map(c -> modifiedExecutables.contains(c) ? null : c)
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
