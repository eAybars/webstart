package com.eaybars.webstart.service.artifact.entity;

import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class ArtifactTest {

    @Test
    public void getTitle() throws Exception {
        Artifact a = new Artifact(URI.create("/my/artifact/test"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/my/artifact/test/"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/test/"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/test"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/my/artifact/test.pdf"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/test.pdf"));
        assertEquals("test", a.getTitle());

        a = new Artifact(URI.create("/my/artifact/test.pdf/"));
        assertEquals("test.pdf", a.getTitle());

        a = new Artifact(URI.create("/my/artifact/test.pdf/img.png"));
        assertEquals("img", a.getTitle());
    }

    @Test
    public void getIcon() throws URISyntaxException {
        Artifact a = new Artifact(URI.create("/my/artifact/test"));
        assertNull(a.getIcon());
        URI uri = new URI("test.png");
        a.setIcon(uri);
        assertEquals(uri, a.getIcon());
        assertEquals(uri.toString(), a.toJson().getString("icon"));
        a.setIcon(null);
        assertNull(a.getIcon());
        assertEquals(JsonValue.NULL, a.toJson().get("icon"));
    }

    @Test
    public void attributes() throws URISyntaxException {
        Artifact a = new Artifact(URI.create("/my/artifact/test"));
        a.attributes().add("someField", "Descriptive text").build();
        assertEquals("Descriptive text", a.toJson().getString("someField"));
    }

    @Test
    public void emptyAttributes() throws URISyntaxException {
        Artifact a = new Artifact(URI.create("/my/artifact/test"));
        a.attributes().add("someField", "Descriptive text").build();

        a.emptyAttributes().build();
        assertNull(a.toJson().get("someField"));
        assertEquals("/my/artifact/test", a.toJson().getString("identifier"));
    }

    @Test
    public void toJsonTest() throws URISyntaxException {
        Artifact a = new Artifact(URI.create("/my/artifact/test"));
        JsonObject json = a.toJson();
        assertEquals(3, json.size());
        assertEquals("artifact", json.getString("type"));
        assertEquals("/my/artifact/test", json.getString("identifier"));
        assertEquals("test", json.getString("title"));
    }

    @Test
    public void equalTest() throws Exception {
        Artifact a1 = new Artifact(URI.create("/my/artifact/test"));
        Artifact a2 = new Artifact(URI.create("/my/artifact/test"));

        assertEquals(a1, a2);

        a2 = new Artifact(URI.create("/my/artifact/test/"));

        assertNotEquals(a1, a2);
    }

    @Test
    public void compareToTest() throws Exception {
        Artifact a1 = new Artifact(URI.create("/my/artifact/test/"));
        Artifact a2 = new Artifact(URI.create("/my/artifact/test/other/"));

        assertTrue(a1.compareTo(a2) < 0);
        assertTrue(a2.compareTo(a1) > 0);
        assertTrue(a1.compareTo(new Artifact(URI.create("/my/artifact/test/"))) == 0);
    }

}