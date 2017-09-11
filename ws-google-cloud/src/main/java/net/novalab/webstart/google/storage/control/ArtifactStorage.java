package net.novalab.webstart.google.storage.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import net.novalab.webstart.google.artifact.control.CloudStorageArtifactSupplier;
import net.novalab.webstart.service.artifact.entity.ArtifactEventSummary;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ApplicationScoped
public class ArtifactStorage {

    @Inject
    CloudStorageArtifactSupplier artifactSupplier;
    @Inject
    Bucket bucket;

    public ArtifactEventSummary delete(URI uri) {
        ArtifactEventSummary summary = artifactSupplier.unload(uri);
        bucket.list(Storage.BlobListOption.prefix(uri.getPath().substring(1)))
                .iterateAll()
                .forEach(Blob::delete);
        return summary;
    }

    public ArtifactEventSummary put(URI uri, InputStream stream) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(stream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                return new ArtifactEventSummary();
            } else {
                ArtifactEventSummary summary = artifactSupplier.unload(uri);

                do {
                    String prefix = uri.toString().substring(1);
                    if (prefix.charAt(prefix.length() - 1) != '/') {
                        prefix += '/';
                    }
                    bucket.create(prefix + entry.getName(),
                            new UnclosableInputStream(zipInputStream),
                            Bucket.BlobWriteOption.doesNotExist());
                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                } while (entry != null);

                return summary.merge(artifactSupplier.load(uri));
            }
        }
    }

    private static class UnclosableInputStream extends InputStream {
        private InputStream inputStream;

        public UnclosableInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public int available() throws IOException {
            return inputStream.available();
        }

        @Override
        public synchronized void mark(int readlimit) {
            inputStream.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            inputStream.reset();
        }

        @Override
        public boolean markSupported() {
            return inputStream.markSupported();
        }
    }

}
