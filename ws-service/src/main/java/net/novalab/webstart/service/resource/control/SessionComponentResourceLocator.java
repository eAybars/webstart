package net.novalab.webstart.service.resource.control;

import net.novalab.webstart.service.application.entity.Component;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.util.Optional;

@Decorator
@Priority(10)
public class SessionComponentResourceLocator extends ComponentResourceLocator{
    @Inject
    @Any
    @Delegate
    ComponentResourceLocator componentResourceLocator;

    @Inject
    SessionComponentUpdateTracking sessionComponentUpdateTracking;

    @Override
    public Optional<Component> componentForResource(String path) {
        Optional<Component> optionalComponent = super.componentForResource(path);
        if (path.endsWith(".jnlp")) {
            optionalComponent.ifPresent(sessionComponentUpdateTracking::clearComponentUpdateStatus);
            return optionalComponent;
        } else if (optionalComponent.map(sessionComponentUpdateTracking::isComponentUpdated).orElse(false)) {
            return Optional.empty();
        } else return optionalComponent;
    }
}
