package net.novalab.webstart.service.artifact.entity;

import org.junit.Test;

import java.net.URI;
import java.net.URL;

import static org.junit.Assert.*;

public class AbstractArtifactTest {

    @Test
    public void getTitle() throws Exception {
        Artifact a = new TestArtifact(URI.create("/my/artifact/test"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/my/artifact/test/"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/test/"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/test"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/my/artifact/test.pdf"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/test.pdf"));
        assertEquals("test", a.getTitle());

        a = new TestArtifact(URI.create("/my/artifact/test.pdf/"));
        assertEquals("test.pdf", a.getTitle());

        a = new TestArtifact(URI.create("/my/artifact/test.pdf/img.png"));
        assertEquals("img", a.getTitle());
    }

    @Test
    public void equalTest() {
        Artifact a1 = new TestArtifact(URI.create("/my/artifact/test"));
        Artifact a2 = new TestArtifact(URI.create("/my/artifact/test"));

        assertEquals(a1, a2);

        a2 = new TestArtifact(URI.create("/my/artifact/test/"));

        assertNotEquals(a1, a2);
    }

    private static class TestArtifact extends AbstractArtifact {

        public TestArtifact(URI identifier) {
            super(identifier);
        }

        @Override
        public URL getResource(String path) {
            return null;
        }
    }
}