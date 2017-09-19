package net.novalab.webstart.service.backend.control;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public interface ZipStorage extends Storage {

    @Override
    default boolean store(URI uri, InputStream stream) throws IOException {
        boolean result = true;
        try (ZipInputStream zipInputStream = new ZipInputStream(stream)) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                result = false;
            } else {
                //first remove any existing data
                delete(uri);

                ZipInputStreamWrapper handOverStream = new ZipInputStreamWrapper(stream);
                do {
                    String prefix = uri.toString().substring(1);
                    if (prefix.charAt(prefix.length() - 1) != '/') {
                        prefix += '/';
                    }
                    try {
                        result = storeEntry(new URI(prefix+entry.getName()), handOverStream);
                    } catch (IOException e) {
                        result = false;
                        throw e;
                    } catch (URISyntaxException e) {
                        result = false;
                        Logger.getLogger(ZipStorage.class.getName())
                                .log(Level.SEVERE, "Storing of "+uri+" is failed.", e);
                    }
                    zipInputStream.closeEntry();
                    entry = zipInputStream.getNextEntry();
                }while (result && entry != null);
            }
        } finally {
            if (!result) {
                delete(uri);
            }
        }
        return result;
    }

    boolean storeEntry(URI uri, InputStream stream) throws IOException;
}
