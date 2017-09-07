package net.novalab.webstart.google.discovery.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;

import javax.inject.Inject;
import java.net.URI;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractArtifactCreator implements ArtifactCreator {
    @Inject
    Bucket bucket;

    public Bucket getBucket() {
        return bucket;
    }

    protected Stream<Blob> blobs(URI uri) {
        String path = uri.toString();
        if (path.charAt(path.length() - 1) != '/') {
            throw new IllegalArgumentException(uri + " is not a path");
        }
        String searchPath = path.startsWith("/") ? path.substring(1) : path;

        return StreamSupport.stream(bucket.list(Storage.BlobListOption.currentDirectory(),
                Storage.BlobListOption.fields(
                        Storage.BlobField.UPDATED,
                        Storage.BlobField.CONTENT_TYPE,
                        Storage.BlobField.CONTENT_DISPOSITION,
                        Storage.BlobField.KIND),
                Storage.BlobListOption.prefix(searchPath))
                .iterateAll().spliterator(), false)
                .filter(b -> !b.getName().equals(searchPath));
    }

    protected URI toArtifactURI(Blob b) {
        return URI.create("/" + b.getName());
    }

    protected URI toArtifactURI(URI uri) {
        return uri.toString().startsWith("/") ? uri : URI.create("/"+uri.toString());
    }
}
