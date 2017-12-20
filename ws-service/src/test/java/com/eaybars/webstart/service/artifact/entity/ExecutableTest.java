package com.eaybars.webstart.service.artifact.entity;

import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ExecutableTest {

    @Test
    public void validExecutable() throws URISyntaxException {
        Executable e = new Executable(new URI("/myapp/launch.jnlp"));
        assertEquals(URI.create("/myapp/"), e.getIdentifier());
        assertEquals(URI.create("/myapp/launch.jnlp"), e.getExecutable());

        e = new Executable(new URI("launch.jnlp"));
        assertEquals(URI.create("/"), e.getIdentifier());
        assertEquals(URI.create("launch.jnlp"), e.getExecutable());

        e = new Executable(new URI("/launch.jnlp"));
        assertEquals(URI.create("/"), e.getIdentifier());
        assertEquals(URI.create("/launch.jnlp"), e.getExecutable());
    }

    @Test(expected = URISyntaxException.class)
    public void invalidExecutableURIBeginningTest() throws URISyntaxException {
        Executable e = new Executable(new URI("myapp/launch.jnlp"));
    }

    @Test(expected = URISyntaxException.class)
    public void invalidExecutableURIDirectoryTest() throws URISyntaxException {
        Executable e = new Executable(new URI("/myapp/launch/"));
    }

    @Test(expected = URISyntaxException.class)
    public void invalidExecutableURIDirectory2Tes() throws URISyntaxException {
        Executable e = new Executable(new URI("/myapp/launch.jnlp/"));
    }

    @Test(expected = URISyntaxException.class)
    public void invalidExecutableURIEndingTes() throws URISyntaxException {
        Executable e = new Executable(new URI("/myapp/launch.exe"));
    }

    @Test
    public void toJsonTest() throws URISyntaxException {
        Executable e = new Executable(URI.create("/myapp/launch.jnlp"));
        JsonObject json = e.toJson();
        assertEquals(4, json.size());
        assertEquals("executable", json.getString("type"));
        assertEquals("/myapp/", json.getString("identifier"));
        assertEquals("myapp", json.getString("title"));
        assertEquals("/myapp/launch.jnlp", json.getString("executable"));
    }


    @Test
    public void versionTest() throws URISyntaxException {
        Executable e = new Executable(URI.create("/my/artifact/test.jnlp"));
        assertNull(e.getVersion());
        assertNull(e.toJson().getString("version", null));

        e.setVersion("1.0");
        assertEquals("1.0", e.getVersion());
        assertEquals("1.0", e.toJson().getString("version"));

        e.setVersion(null);
        assertNull(e.getVersion());
        assertNull(e.toJson().getString("version", null));
    }

    @Test
    public void dateModifiedTest() throws URISyntaxException {
        Executable e = new Executable(URI.create("/my/artifact/test.jnlp"));
        assertNull(e.getDateModified());
        assertNull(e.toJson().getString("dateModified", null));

        Date date = new Date();
        e.setDateModified(date);
        assertEquals(date, e.getDateModified());

        e.setDateModified(null);
        assertNull(e.getDateModified());
        assertNull(e.toJson().getString("dateModified", null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void executableTest() throws URISyntaxException {
        Executable e = new Executable(URI.create("/my/artifact/test.jnlp"));
        assertEquals(URI.create("/my/artifact/test.jnlp"), e.getExecutable());
        assertEquals("/my/artifact/test.jnlp", e.toJson().getString("executable"));

        e.attributes().add("executable", "/my/artifact/test2.jnlp");
    }

}
