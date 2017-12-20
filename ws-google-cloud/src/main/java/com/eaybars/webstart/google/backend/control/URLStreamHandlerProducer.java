package com.eaybars.webstart.google.backend.control;

import com.eaybars.webstart.service.backend.control.Backend;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.channels.Channels;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ApplicationScoped
public class URLStreamHandlerProducer {
    static final DateFormat DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss X");

    @Inject
    Bucket bucket;

    private URLStreamHandler handler = new GCSStreamHandler();

    @Produces
    public URLStreamHandler getUrlStreamHandler() {
        return handler;
    }

    private class GCSStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new GCSURLConnection(u);
        }
    }

    private class GCSURLConnection extends URLConnection {
        private Blob blob;
        /**
         * Constructs a URL connection to the specified URL. A connection to
         * the object referenced by the URL is not created.
         *
         * @param url the specified URL.
         */
        protected GCSURLConnection(URL url) {
            super(url);
        }


        @Override
        public long getLastModified() {
            try {
                connect();
                return blob.getUpdateTime();
            } catch (IOException e) {
                return 0;
            }
        }

        @Override
        public String getHeaderField(String name) {
            try {
                connect();
            } catch (IOException e) {
                return super.getHeaderField(name);
            }

            switch (name) {
                case "content-type": return blob.getContentType();
                case "content-length": return blob.getSize().toString();
                case "content-encoding": return blob.getContentEncoding();
                case "date": return DATE_FORMAT.format(new Date(blob.getCreateTime()));
                case "last-modified": return DATE_FORMAT.format(new Date(blob.getUpdateTime()));
                default: return blob.getMetadata().getOrDefault(name, super.getHeaderField(name));
            }
        }

        @Override
        public void connect() throws IOException {
            if (!connected){
                try {
                    blob = bucket.get(Backend.ROOT.relativize(URI.create(url.getPath())).getPath());
                } catch (Exception e) {
                    throw new IOException(e);
                }
                if (blob == null) {
                    throw new IOException("Cloud storage object is missing for: " + getURL().getPath());
                }
                connected = true;
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            connect();
            return Channels.newInputStream(blob.reader());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            connect();
            return Channels.newOutputStream(blob.writer());
        }
    }

}
