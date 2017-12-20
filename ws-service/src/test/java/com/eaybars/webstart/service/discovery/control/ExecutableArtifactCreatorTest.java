package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Executable;
import com.eaybars.webstart.service.backend.control.Backend;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExecutableArtifactCreatorTest {

    private ExecutableArtifactCreator creator;
    private Backend backend;


    @Before
    public void setUp() {
        creator = new ExecutableArtifactCreator();
        backend = Mockito.mock(Backend.class);
    }

    @Test
    public void applyWithNoExecutable() {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/resource.pdf"),
                URI.create("/path/to/icon.png")
        ));
        assertEquals(0, stream.count());
    }

    @Test
    public void applyWithOneExecutable() {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/resource.pdf"),
                URI.create("/path/to/launch.jnlp"),
                URI.create("/path/to/icon.png")
        ));
        Set<Artifact> set = stream.collect(Collectors.toSet());
        assertEquals(1, set.size());

        Artifact artifact = set.iterator().next();
        assertTrue(artifact instanceof Executable);
        assertEquals(URI.create("/path/to/"), artifact.getIdentifier());

        assertEquals(URI.create("/path/to/launch.jnlp"), ((Executable) artifact).getExecutable());
    }

    @Test
    public void applyWithTwoExecutable() {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/resource.pdf"),
                URI.create("/path/to/launch.jnlp"),
                URI.create("/path/to/icon.png"),
                URI.create("/path/to/build.jnlp")
        ));
        Set<Artifact> set = stream.collect(Collectors.toSet());
        assertEquals(1, set.size());

        Artifact artifact = set.iterator().next();
        assertTrue(artifact instanceof Executable);
        assertEquals(URI.create("/path/to/"), artifact.getIdentifier());

        assertEquals(URI.create("/path/to/build.jnlp"), ((Executable) artifact).getExecutable());
    }
}