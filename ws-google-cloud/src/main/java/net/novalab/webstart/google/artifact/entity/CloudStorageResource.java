package net.novalab.webstart.google.artifact.entity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class CloudStorageResource extends CloudStorageArtifact {
    public CloudStorageResource(URI identifier) {
        super(identifier);
    }

    @Override
    public URL getResource(String path) {
        try {
            return "".equals(path) ? toURL(path) : null;
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
