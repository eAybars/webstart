package com.eaybars.webstart.file.artifact.entity;

import com.eaybars.webstart.service.artifact.entity.Component;

import java.io.File;
import java.net.URI;
import java.net.URL;

public class FileBasedComponent extends FileBasedArtifact implements Component {

    public FileBasedComponent(URI identifier, File identifierFile) {
        super(identifier, identifierFile);
        if (!identifierFile.isDirectory()) {
            throw new IllegalArgumentException(identifier + " is not a directory");
        }
    }

    @Override
    public URL getResource(String path) {
        if (path == null || "".equals(path) ||
                path.matches(".*\\.\\./.*")) {//if contains ../ skip it for security reasons
            return null;
        }
        return toResourceURL(new File(getIdentifierFile(), path));
    }

}
