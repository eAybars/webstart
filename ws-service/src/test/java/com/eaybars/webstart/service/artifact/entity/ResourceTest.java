package com.eaybars.webstart.service.artifact.entity;

import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;


public class ResourceTest {

    @Test
    public void validIdentifierURITest() throws Exception {
        new Resource(new URI("/resources/documents/tutorial.pdf?v=1_0"));
        new Resource(new URI("/resources/documents/tutorial.pdf"));
    }

    @Test(expected = URISyntaxException.class)
    public void invalidIdentifierURITest() throws Exception {
        new Resource(new URI("/resources/documents/tutorial.pdf/abc"));
    }

    @Test
    public void toJsonTest() throws Exception {
        Resource r = new Resource(new URI("/resources/documents/tutorial.pdf?v=1_0"));
        JsonObject json = r.toJson();

        assertEquals(3, json.size());
        assertEquals("/resources/documents/tutorial.pdf?v=1_0", json.getString("identifier"));
        assertEquals("tutorial", json.getString("title"));
        assertEquals("resource", json.getString("type"));
        assertNull(json.get("size"));
    }

    @Test
    public void sizeTest() throws Exception {
        Resource r = new Resource(new URI("/resources/documents/tutorial.pdf?v=1_0"));
        r.setSize(100);
        assertEquals(100, r.getSize());

        r.attributes().add("size", 350).build();

        assertEquals(350, r.getSize());
        try {
            r.attributes().add("size", "350").build();
            fail();
        } catch (IllegalArgumentException e) {

        }

        try {
            r.attributes().addNull("size").build();
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
}