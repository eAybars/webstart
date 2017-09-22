package net.novalab.webstart.service.uri.control.protocol.gcs;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import net.novalab.webstart.service.uri.control.CDIDelegatingURLStreamHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;

public class Handler extends CDIDelegatingURLStreamHandler {
    public static final String PROTOCOL_NAME = "gcs";

    public static final Handler INSTANCE = new Handler();

    @ApplicationScoped
    public static class CDIGCSHandler extends CDIDelegatingURLStreamHandler.CDIDelegate {
        @Inject
        Bucket bucket;

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new URLConnectionUmpl(bucket, u);
        }
    }


    private static class URLConnectionUmpl extends URLConnection {
        private Bucket bucket;
        private Blob blob;

        /**
         * Constructs a URL connection to the specified URL. A connection to
         * the object referenced by the URL is not created.
         *
         * @param url the specified URL.
         */
        protected URLConnectionUmpl(Bucket bucket, URL url) {
            super(url);
            this.bucket = bucket;
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
                case "date": return blob.getCreateTime().toString();
                case "last-modified": return blob.getUpdateTime().toString();
                default: return blob.getMetadata().getOrDefault(name, super.getHeaderField(name));
            }
        }

        @Override
        public void connect() throws IOException {
            if (!connected){
                try {
                    blob = bucket.get(url.getPath().substring(1));
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
