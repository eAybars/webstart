package net.novalab.webstart.file.backend.control;

import net.novalab.webstart.service.backend.control.ZipStorage;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@ApplicationScoped
public class FileStorage implements ZipStorage {

    @Override
    public boolean storeEntry(URI uri, InputStream stream) throws IOException {
        return false;
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        return false;
    }
}
