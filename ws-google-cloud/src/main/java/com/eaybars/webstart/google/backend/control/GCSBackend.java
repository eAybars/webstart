package com.eaybars.webstart.google.backend.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;

import static com.google.cloud.storage.Storage.*;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Component;
import com.eaybars.webstart.service.artifact.entity.Executable;
import com.eaybars.webstart.service.artifact.entity.Resource;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Storage;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class GCSBackend implements Backend {
    public static final URI NAME = URI.create("/gcs/");
    private static final Function<URI, Artifact> NULL_CREATOR = u -> null;

    @Inject
    Bucket bucket;

    @Inject
    GCSStorage storage;

    private Map<Class<?>, Function<URI, Artifact>> creators = new HashMap<>();

    @PostConstruct
    public void initCreators() {
        creators.put(Component.class, new ComponentCreator(this));
        creators.put(Executable.class, new ExecutableCreator(this));
        creators.put(Resource.class, new ResourceCreator(this));
    }

    @Override
    public URI getName() {
        return NAME;
    }

    @Override
    public Stream<URI> contents(URI parent) {
        return blobContents(parent, BlobListOption.currentDirectory())
                .map(Blob::getName)
                .map(URI::create);
    }

    public Stream<Blob> blobContents(URI parent, BlobListOption... options) {
        String path = parent.toString();
        if (!isDirectory(parent)) {
            throw new IllegalArgumentException(parent + " is not a path");
        }
        String searchPath = path.startsWith("/") ? path.substring(1) : path;

        Set<BlobListOption> allOptions = Stream.of(options).collect(Collectors.toSet());
        allOptions.add(BlobListOption.prefix(searchPath));

        return StreamSupport.stream(bucket.list(
                allOptions.toArray(new BlobListOption[allOptions.size()]))
                .iterateAll().spliterator(), false)
                .filter(b -> !b.getName().equals(searchPath));
    }

    @Override
    public <T extends Artifact> T createArtifact(Class<T> type, URI target) {
        return (T) creators.getOrDefault(type, NULL_CREATOR).apply(target);
    }

    @Override
    public Optional<Storage> getStorage() {
        return Optional.of(storage);
    }
}
