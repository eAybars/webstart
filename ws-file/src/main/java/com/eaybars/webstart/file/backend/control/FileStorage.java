package com.eaybars.webstart.file.backend.control;

import com.eaybars.webstart.service.backend.control.Storage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@ApplicationScoped
public class FileStorage implements Storage {
    @Inject
    FileBackend backend;

    @Override
    public boolean store(URI uri, InputStream stream) throws IOException {
        Files.copy(stream, backend.toFile(uri).toPath(), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return delete(backend.toFile(uri));
    }

    private boolean delete(File file) {
        if (file.isDirectory()) {
            return Stream.of(file.listFiles())
                    .map(this::delete)
                    .reduce(Boolean::logicalAnd)
                    .orElse(true) && file.delete();
        } else
            return file.delete();
    }
}
