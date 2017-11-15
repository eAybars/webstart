package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Resource;
import com.eaybars.webstart.service.backend.control.Backends;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApplicationScoped
public class PdfArtifactDiscovery implements ArtifactDiscovery {
    @Override
    public Stream<Artifact> apply(Backends.BackendURI backendURI) {
        return backendURI.getBackend().contents(backendURI.getUri())
                .filter(((Predicate<URI>)backendURI.getBackend()::isDirectory).negate())
                .filter(c -> c.getPath().endsWith(".pdf"))
                .map(c -> (Artifact)backendURI.getBackend().createArtifact(Resource.class, c))
                .filter(Objects::nonNull);
    }
}
