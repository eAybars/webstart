package net.novalab.webstart.service.artifact.entity;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Optional;

import static org.junit.Assert.*;

public class ComponentTest {
    private Component component;
    private URI identifier;

    @Before
    public void setUp() throws Exception {
        component = new TestComponent(new URI("/components/sub-components/1/"));
    }

    @Test
    public void resolve() throws Exception {
        URI resolve = component.resolve(new URI("images/image.png"));
        assertEquals("/components/sub-components/1/images/image.png", resolve.toString());

        resolve = component.resolve(new URI("/images/image.png"));
        assertEquals("/images/image.png", resolve.toString());
    }

    @Test
    public void toRelativePath() throws Exception {
        Optional<String> path = component.toRelativePath(new URI("/components/sub-components/1/images/image.png"));
        assertNotNull(path);
        assertTrue(path.isPresent());
        assertEquals("images/image.png", path.get());

        path = component.toRelativePath(new URI("components/sub-components/1/images/image.png"));
        assertNotNull(path);
        assertFalse(path.isPresent());

        path = component.toRelativePath(new URI("/images/image.png"));
        assertNotNull(path);
        assertFalse(path.isPresent());
    }

    private static class TestComponent extends AbstractArtifact implements Component {

        public TestComponent(URI identifier) {
            super(identifier);
        }

        @Override
        public URL getResource(String path) {
            return null;
        }
    }
}