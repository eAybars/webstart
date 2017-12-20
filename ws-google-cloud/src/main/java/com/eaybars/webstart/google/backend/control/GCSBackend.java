package com.eaybars.webstart.google.backend.control;

import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Storage;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.cloud.storage.Storage.BlobListOption;

@ApplicationScoped
public class GCSBackend implements Backend {
    public static final String PROTOCOL_NAME = "gcs";
    public static final URI NAME = URI.create("/" + PROTOCOL_NAME + "/");

    @Inject
    Bucket bucket;

    @Inject
    URLStreamHandler handler;

    @Inject
    GCSStorage storage;


    @Override
    public URI getName() {
        return NAME;
    }

    @Override
    public Stream<URI> contents(URI parent) {
        return isDirectory(parent) ?
                blobContents(Backend.ROOT.relativize(parent), BlobListOption.currentDirectory())
                        .map(Blob::getName)
                        .map(URI::create)
                        .map(Backend.ROOT::resolve)
                : Stream.empty();
    }

    @Override
    public URL getResource(URI uri) {
        try {
            URL url = new URL(null, PROTOCOL_NAME + "://" + Backend.ROOT.resolve(uri).toString(), handler);
            url.openConnection().connect();
            return url;
        } catch (IOException e) {
            return null;
        }
    }

    private Stream<Blob> blobContents(URI parent, BlobListOption... options) {
        String searchPath = Backend.ROOT.relativize(parent).getPath();

        Set<BlobListOption> allOptions = Stream.of(options).collect(Collectors.toSet());
        allOptions.add(BlobListOption.prefix(searchPath));

        return StreamSupport.stream(bucket.list(
                allOptions.toArray(new BlobListOption[allOptions.size()]))
                .iterateAll().spliterator(), false)
                .filter(b -> !b.getName().equals(searchPath));
    }


    @Override
    public Optional<Storage> getStorage() {
        return Optional.of(storage);
    }
}
