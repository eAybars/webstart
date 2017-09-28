package com.eaybars.webstart.file.artifact.entity;

import com.eaybars.webstart.service.artifact.entity.Resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class FileBasedResource extends FileBasedArtifact implements Resource {
    public FileBasedResource(URI identifier, File identifierFile) {
        super(identifier, identifierFile);
        if (identifierFile.isDirectory()) {
            throw new IllegalArgumentException("Resource file cannot be a directory: " + identifierFile);
        }
    }

    @Override
    public URL getResource(String path) {
        if ("".equals(path)) {
            try {
                return getIdentifierFile().toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        } else {
            return toResourceURL(new File(getIdentifierFile().getParentFile(), path));
        }
    }
}
