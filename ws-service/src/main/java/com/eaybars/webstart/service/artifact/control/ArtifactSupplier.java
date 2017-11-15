package com.eaybars.webstart.service.artifact.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by ertunc on 30/05/17.
 */
public interface ArtifactSupplier extends Supplier<Stream<Artifact>> {
}
