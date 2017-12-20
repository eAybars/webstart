package com.eaybars.webstart.google.backend.control;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.internal.matchers.VarargMatcher;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GCSBackendTest {

    private GCSBackend backend;
    private URI absolute, relative;
    private URLConnection validConnection, invalidConnection;

    @Before
    public void setUp() throws Exception {
        backend = new GCSBackend();
        backend.bucket = mock(Bucket.class);
        validConnection = mock(URLConnection.class);
        invalidConnection = mock(URLConnection.class);
        backend.handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return u.toString().equals("gcs:/path/to/app.jar") ? validConnection : invalidConnection;
            }
        };
        doThrow(new IOException()).when(invalidConnection).connect();

        absolute = new URI("/path/to/test/");
        relative = new URI("path/to/test/");

        Blob b1 = mock(Blob.class);
        when(b1.getName()).thenReturn("b1");
        Blob b2 = mock(Blob.class);
        when(b2.getName()).thenReturn("b2/");
        Blob b3 = mock(Blob.class);
        when(b3.getName()).thenReturn("b3/b4");

        Page<Blob> page = mock(Page.class);
        when(page.iterateAll()).thenReturn(Arrays.asList(b1, b2, b3));

        when(backend.bucket.list(argThat(new OptionMatcher()))).thenReturn(page);
    }

    @Test
    public void contents() throws Exception {
        verifyContents(backend.contents(absolute));
        verifyContents(backend.contents(relative));

        assertFalse(backend.contents(new URI("/path/to/test")).findAny().isPresent());
    }

    private void verifyContents(Stream<URI> contents) {
        Set<URI> uris = contents.collect(Collectors.toSet());
        assertEquals(3, uris.size());
        assertTrue(uris.contains(URI.create("/b1")));
        assertTrue(uris.contains(URI.create("/b2/")));
        assertTrue(uris.contains(URI.create("/b3/b4")));
    }

    @Test
    public void getResource() throws IOException {
        URL resource = backend.getResource(URI.create("/test/to/resource.pdf"));
        assertNull(resource);
        verify(invalidConnection).connect();
        verifyZeroInteractions(validConnection);

        resource = backend.getResource(URI.create("/path/to/app.jar"));
        assertNotNull(resource);
        verify(invalidConnection).connect();
        verify(validConnection).connect();

        resource = backend.getResource(URI.create("path/to/app.jar"));
        assertNotNull(resource);
        verify(invalidConnection).connect();
        verify(validConnection, times(2)).connect();
    }

    private static class OptionMatcher extends ArgumentMatcher<Storage.BlobListOption[]> implements VarargMatcher {

        @Override
        public boolean matches(Object argument) {
            return Arrays.asList((Storage.BlobListOption[]) argument)
                    .contains(Storage.BlobListOption.prefix("path/to/test/"));
        }
    }
}