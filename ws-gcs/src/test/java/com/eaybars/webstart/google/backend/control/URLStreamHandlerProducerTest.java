package com.eaybars.webstart.google.backend.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class URLStreamHandlerProducerTest {
    private URLStreamHandlerProducer producer;
    private Blob blob;
    private Date dateModified;

    @Before
    public void setUp() {
        producer = new URLStreamHandlerProducer();
        producer.bucket = mock(Bucket.class);

        blob = mock(Blob.class);
        when(blob.getContentType()).thenReturn("application/json");
        when(blob.getSize()).thenReturn(12345L);
        when(blob.getContentEncoding()).thenReturn("utf-8");
        when(blob.getCreateTime()).thenReturn(3600000L);
        when(blob.getUpdateTime()).thenReturn((dateModified = new Date()).getTime());

        when(producer.bucket.get("path/to/test")).thenReturn(blob);
    }

    private URL createURL(String path) throws MalformedURLException {
        return new URL(null, GCSBackend.PROTOCOL_NAME + "://" + path, producer.getUrlStreamHandler());
    }

    @Test
    public void getUrlStreamHandler() {
        URLStreamHandler handler = producer.getUrlStreamHandler();
        assertNotNull(handler);
    }

    @Test
    public void openConnection() throws Exception {
        URL url = createURL("/path/to/test");
        URLConnection connection = url.openConnection();
        assertNotNull(connection);
        verifyZeroInteractions(producer.bucket);
    }

    @Test(expected = IOException.class)
    public void connect() throws Exception {
        URL url = createURL("/path/to/test");
        URLConnection connection = url.openConnection();
        connection.connect();
        verify(producer.bucket).get("path/to/test");
        connection.connect();
        verify(producer.bucket).get("path/to/test");//not invoked second time


        url = createURL("/path/to/nothing");
        connection = url.openConnection();
        connection.connect();//IOException
    }

    @Test()
    public void getHeaderField() throws Exception {
        URL url = createURL("/path/to/test");
        URLConnection connection = url.openConnection();

        assertEquals("application/json", connection.getHeaderField("content-type"));
        verify(blob).getContentType();

        assertEquals("12345", connection.getHeaderField("content-length"));
        verify(blob).getSize();

        assertEquals("utf-8", connection.getHeaderField("content-encoding"));
        verify(blob).getContentEncoding();

        assertEquals(URLStreamHandlerProducer.DATE_FORMAT.format(new Date(3600000L)), connection.getHeaderField("date"));
        verify(blob).getCreateTime();

        assertEquals(URLStreamHandlerProducer.DATE_FORMAT.format(dateModified), connection.getHeaderField("last-modified"));
        verify(blob).getUpdateTime();

        verifyNoMoreInteractions(blob);
    }

    @Test
    public void getLastModified() throws Exception {
        URL url = createURL("/path/to/test");

        URLConnection connection = url.openConnection();
        assertEquals(dateModified.getTime(), connection.getLastModified());

        url = createURL("path/to/test");
        connection = url.openConnection();
        assertEquals(0, connection.getLastModified());
    }


}