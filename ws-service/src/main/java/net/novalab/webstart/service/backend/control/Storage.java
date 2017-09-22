package net.novalab.webstart.service.backend.control;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public interface Storage {

    boolean delete(URI uri) throws IOException;

    boolean store(URI uri, InputStream is) throws IOException;

    default boolean storeZip(URI uri, InputStream stream) throws IOException {
        boolean result = true;
        try (ZipInputStream zipInputStream = new ZipInputStream(stream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                result = false;
            } else {

                do {
                    String prefix = uri.toString();
                    if (!"".equals(prefix) && prefix.charAt(prefix.length() - 1) != '/') {
                        prefix += '/';
                    }
                    try (InputStream handOverStream = new BufferedInputStream(new NonClosingInputStream(zipInputStream))) {
                        result = store(new URI(prefix + entry.getName()), handOverStream);
                    } catch (IOException e) {
                        result = false;
                        throw e;
                    } catch (URISyntaxException e) {
                        result = false;
                        Logger.getLogger(Storage.class.getName())
                                .log(Level.SEVERE, "Storing of " + uri + " is failed.", e);
                    }
                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                } while (result && entry != null);
            }
        }
        return result;
    }
}
