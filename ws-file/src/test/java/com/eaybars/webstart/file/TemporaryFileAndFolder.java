package com.eaybars.webstart.file;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class TemporaryFileAndFolder extends TemporaryFolder {

    public File createFile(File folder, String name) throws IOException {
        File file = new File(folder, name);
        if (!file.createNewFile()) {
            throw new IOException("Cannot create file: "+file);
        }
        return file;
    }
}
