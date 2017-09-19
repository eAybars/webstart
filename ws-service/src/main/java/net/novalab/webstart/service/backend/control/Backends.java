package net.novalab.webstart.service.backend.control;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class Backends {
    @Inject
    @Any
    Instance<Backend> backendInstances;

    public Optional<BackendURI> toBackendURI(URI uri) {
        return stream()
                .sorted(Comparator.comparing(Backend::getName))
                .filter(b -> !uri.equals(b.getName().relativize(uri)))
                .map(b -> new BackendURI(b, b.getName().relativize(uri)))
                .findFirst();
    }

    public Stream<Backend> stream() {
        return StreamSupport.stream(backendInstances.spliterator(), false);
    }

    public static class BackendURI {
        private Backend backend;
        private URI uri;

        private BackendURI(Backend backend, URI uri) {
            this.backend = backend;
            this.uri = uri;
        }

        public BackendURI newBackendURI(URI uri) {
            return new BackendURI(getBackend(), getBackend().getName().relativize(uri));
        }

        public Backend getBackend() {
            return backend;
        }

        public URI getUri() {
            return uri;
        }

        public boolean isDirectory() {
            return backend.isDirectory(uri);
        }

        @Override
        public String toString() {
            return getUri() + " @ " + getBackend().getName();
        }
    }
}
