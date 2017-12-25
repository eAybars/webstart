package com.eaybars.webstart.file.backend.control;

import com.eaybars.webstart.file.TemporaryFileAndFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class FileBackendTest {
    private FileBackend fileBackend;
    private File app2Folder, resourceFile;

    @Rule
    public TemporaryFileAndFolder temporaryFolder = new TemporaryFileAndFolder();

    @Before
    public void setUp() throws Exception {
        fileBackend = new FileBackend();
        fileBackend.root = temporaryFolder.getRoot();

        temporaryFolder.createFile(temporaryFolder.newFolder("app-1"), "launch.jnlp");
        temporaryFolder.newFile("resource.pdf");
        app2Folder = temporaryFolder.newFolder("group", "app2");
        temporaryFolder.createFile(app2Folder, "launch.jnlp");
        resourceFile = temporaryFolder.createFile(app2Folder, "icon.png");
    }

    @Test
    public void contents() throws Exception {
        Set<URI> uris = fileBackend.contents(URI.create("/")).collect(Collectors.toSet());
        assertEquals(3, uris.size());
        assertTrue(uris.contains(URI.create("/app-1/")));
        assertTrue(uris.contains(URI.create("/group/")));
        assertTrue(uris.contains(URI.create("/resource.pdf")));

        uris = fileBackend.contents(URI.create("/app-1/")).collect(Collectors.toSet());
        assertEquals(Collections.singleton(URI.create("/app-1/launch.jnlp")), uris);

        uris = fileBackend.contents(URI.create("app-1/")).collect(Collectors.toSet());
        assertEquals(Collections.singleton(URI.create("/app-1/launch.jnlp")), uris);

        uris = fileBackend.contents(URI.create("/app-1")).collect(Collectors.toSet());
        assertEquals(Collections.singleton(URI.create("/app-1/launch.jnlp")), uris);

        uris = fileBackend.contents(URI.create("/resource.pdf")).collect(Collectors.toSet());
        assertEquals(Collections.emptySet(), uris);
    }

    @Test
    public void getResource() {
        URL resource = fileBackend.getResource(URI.create("/resource.pdf"));
        assertNotNull(resource);

        assertEquals(resource, fileBackend.getResource(URI.create("resource.pdf")));

        resource = fileBackend.getResource(URI.create("/app-1"));
        assertNull(resource);

        resource = fileBackend.getResource(URI.create("/group/app2/icon.png"));
        assertNotNull(resource);
    }

    @Test
    public void toURI() {
        assertEquals(URI.create("/group/app2/"), fileBackend.toURI(app2Folder));
        assertEquals(URI.create("/group/app2/icon.png"), fileBackend.toURI(resourceFile));
    }

    @Test
    public void toFile() {
        assertEquals(app2Folder, fileBackend.toFile(URI.create("/group/app2/")));
        assertEquals(resourceFile, fileBackend.toFile(URI.create("/group/app2/icon.png")));
    }
}