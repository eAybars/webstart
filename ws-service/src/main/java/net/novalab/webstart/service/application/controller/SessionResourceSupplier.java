package net.novalab.webstart.service.application.controller;

import net.novalab.webstart.service.application.entity.Executable;
import net.novalab.webstart.service.authorization.control.AuthorizationControl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by ertunc on 31/05/17.
 */
@ApplicationScoped
public class SessionResourceSupplier {
    @Inject
    Instance<ComponentSupplier> componentSuppliers;
    @Inject
    AuthorizationControl authorizationControl;


    @Produces
    @SessionScoped
    public SessionResourceLocator getSessionResourceLocator() {
        return new SessionResourceLocator(StreamSupport.stream(componentSuppliers.spliterator(), false)
                .flatMap(ComponentSupplier::get)
                .filter(Executable.class::isInstance)
                .filter(authorizationControl)
                .map(Executable.class::cast)
                .collect(Collectors.toList())
        );
    }

    public static class SessionResourceLocator implements Function<String, URL>, Serializable {
        private List<Executable> executables;

        public SessionResourceLocator(List<Executable> executables) {
            this.executables = executables;
        }

        @Override
        public URL apply(String s) {
            return executables.stream()
                    .map(e -> e.getResource(s))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
    }

}
