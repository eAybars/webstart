package com.eaybars.webstart.service.artifact.entity;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.*;

public class ResourceTest {
    private Resource resource;
    private URI identifier;

    @Before
    public void setUp() throws Exception {
        resource = new TestResource(new URI("/resources/documents/tutorial.pdf?v=1_0"));
    }

    @Test
    public void resolve() throws Exception {
        URI resolve = resource.resolve(new URI("images/icon.png"));
        assertEquals("/resources/documents/images/icon.png", resolve.toString());

        resolve = resource.resolve(new URI("/images/icon.png"));
        assertEquals("/images/icon.png", resolve.toString());

        resource = new TestResource(new URI("/resources/documents/tutorial.pdf"));
        resolve = resource.resolve(new URI("images/icon.png"));
        assertEquals("/resources/documents/images/icon.png", resolve.toString());

        resolve = resource.resolve(new URI("/images/icon.png"));
        assertEquals("/images/icon.png", resolve.toString());
    }

    @Test
    public void toRelativePath() throws Exception {
        Optional<String> path = resource.toRelativePath(new URI("/resources/documents/images/icon.png"));
        assertNotNull(path);
        assertFalse(path.isPresent());

        path = resource.toRelativePath(new URI("/resources/documents/tutorial.pdf"));
        assertNotNull(path);
        assertTrue(path.isPresent());
        assertEquals("", path.get());

        path = resource.toRelativePath(new URI("/resources/documents/tutorial.pdf?v=1_0"));
        assertNotNull(path);
        assertTrue(path.isPresent());
        assertEquals("?v=1_0", path.get());

        path = resource.toRelativePath(new URI("/resources/documents/tutorial.pdf?v=2_0"));
        assertNotNull(path);
        assertTrue(path.isPresent());
        assertEquals("?v=2_0", path.get());
    }

    private static class TestResource extends AbstractArtifact implements Resource {

        public TestResource(URI identifier) {
            super(identifier);
        }

        @Override
        public URL getResource(String path) {
            return null;
        }
    }
}