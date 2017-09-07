package net.novalab.webstart.google.artifact.entity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class CloudStorageComponent extends CloudStorageArtifact {

    private Set<String> resources;

    public CloudStorageComponent(URI identifier) {
        super(identifier);
        resources = new HashSet<>();
    }

    public Set<String> getResources() {
        return resources;
    }

    @Override
    public URL getResource(String path) {
        String blobName = getIdentifier().toString() + path;
        if (getResources().contains(blobName)) {
            try {
                return toURL(blobName);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }
}
