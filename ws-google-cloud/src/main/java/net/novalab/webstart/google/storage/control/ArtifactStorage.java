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
import java.util.zip.GZIPInputStream;
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
        try (ZipInputStream zipInputStream = new ZipInputStream(stream)){
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                return new ArtifactEventSummary();
            } else {
                ArtifactEventSummary summary = artifactSupplier.unload(uri);

                do {
                    bucket.create(entry.getName(), zipInputStream);
                    entry = zipInputStream.getNextEntry();
                } while (entry != null);

                return summary.merge(artifactSupplier.load(uri));
            }
        }
    }




}
