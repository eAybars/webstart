package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backends;

import java.util.function.Function;
import java.util.stream.Stream;

public interface ArtifactDiscovery extends Function<Backends.BackendURI, Stream<? extends Artifact>> {
}
