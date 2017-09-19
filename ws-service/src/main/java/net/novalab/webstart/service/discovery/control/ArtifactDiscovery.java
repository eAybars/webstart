package net.novalab.webstart.service.discovery.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.backend.control.Backends;

import java.util.function.Function;
import java.util.stream.Stream;

public interface ArtifactDiscovery extends Function<Backends.BackendURI, Stream<? extends Artifact>> {
}
