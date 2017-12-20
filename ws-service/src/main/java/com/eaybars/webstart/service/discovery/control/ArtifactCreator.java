package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backend;

import java.net.URI;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface ArtifactCreator extends BiFunction<Backend, List<URI>, Stream<Artifact>> {
}
