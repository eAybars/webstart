package com.eaybars.webstart.file.backend.control;

import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Storage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FileBackend implements Backend {
    public static final URI NAME = URI.create("/local/");

    @Inject
    @ArtifactRoot
    File root;

    @Inject
    FileStorage fileStorage;


    @Override
    public URI getName() {
        return NAME;
    }

    @Override
    public Stream<URI> contents(URI parent) {
        File file = toFile(parent);
        if (file.isDirectory()) {
            return Stream.of(file.listFiles())
                    .map(this::toURI);
        }
        return Stream.empty();
    }

    public File toFile(URI uri) {
        return new File(root, Backend.ROOT.resolve(uri).toString());
    }

    public URI toURI(File file) {
        URI relative = root.toURI().relativize(file.toURI());
        if (relative.equals(file.toURI())) {
            throw new IllegalArgumentException(file + " is not a sub component of " + root);
        }
        return Backend.ROOT.resolve(relative);
    }

    @Override
    public URL getResource(URI uri) {
        File file = toFile(uri);
        try {
            return file.exists() && !file.isDirectory() ? file.toURI().toURL() : null;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public Optional<Storage> getStorage() {
        return Optional.of(fileStorage);
    }

}
