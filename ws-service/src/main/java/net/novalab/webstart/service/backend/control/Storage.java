package net.novalab.webstart.service.backend.control;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface Storage {

    boolean delete(URI uri) throws IOException;;

    boolean store(URI uri, InputStream is) throws IOException;
}
