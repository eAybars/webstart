package com.eaybars.webstart.google.backend.control;

import com.eaybars.webstart.service.backend.control.Storage;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.StorageException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class GCSStorage implements Storage {
    @Inject
    Bucket bucket;


    @Override
    public boolean store(URI uri, InputStream stream) throws IOException {
        try {
            bucket.create(uri.toString(), stream);
            return true;
        } catch (StorageException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return StreamSupport.stream(bucket.list(
                com.google.cloud.storage.Storage.BlobListOption.prefix(uri.getPath()))
                .iterateAll().spliterator(), false)
                .map(Blob::delete)
                .reduce(Boolean::logicalAnd)
                .orElse(false);
    }
}
