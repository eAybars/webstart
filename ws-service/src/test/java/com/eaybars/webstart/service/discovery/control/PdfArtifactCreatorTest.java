package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Resource;
import com.eaybars.webstart.service.backend.control.Backend;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PdfArtifactCreatorTest {
    private PdfArtifactCreator creator;
    private Backend backend;

    @Before
    public void setUp() throws Exception {
        creator = new PdfArtifactCreator();
        backend = Mockito.mock(Backend.class);
    }

    @Test
    public void applyWithNoPdf() {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/launch.jnlp"),
                URI.create("/path/to/icon.png")
        ));
        assertEquals(0, stream.count());
    }

    @Test
    public void applyWithOnePdf() {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/launch.jnlp"),
                URI.create("/path/to/tutorial.pdf"),
                URI.create("/path/to/icon.png")
        ));
        Set<Artifact> set = stream.collect(Collectors.toSet());
        assertEquals(1, set.size());

        Artifact artifact = set.iterator().next();
        assertTrue(artifact instanceof Resource);
        assertEquals(URI.create("/path/to/tutorial.pdf"), artifact.getIdentifier());
    }

    @Test
    public void applyWithMultiplePdf() throws URISyntaxException {
        Stream<Artifact> stream = creator.apply(backend, Arrays.asList(
                URI.create("/path/to/directory"),
                URI.create("/path/to/manual.pdf"),
                URI.create("/path/to/launch.jnlp"),
                URI.create("/path/to/tutorial.pdf"),
                URI.create("/path/to/icon.png")
        ));
        Set<Artifact> set = stream.collect(Collectors.toSet());
        assertEquals(2, set.size());

        assertTrue(set.contains(new Resource(URI.create("/path/to/tutorial.pdf"))));
        assertTrue(set.contains(new Resource(URI.create("/path/to/manual.pdf"))));
    }
}