package net.novalab.webstart.service.artifact.control;

import net.novalab.webstart.service.artifact.entity.Artifact;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by ertunc on 30/05/17.
 */
public interface ArtifactSupplier extends Supplier<Stream<? extends Artifact>> {
}
