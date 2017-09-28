package com.eaybars.webstart.google.artifact.entity;

import com.eaybars.webstart.service.artifact.entity.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class GCSComponent extends GCSArtifact implements Component {

    private Set<String> resources;

    public GCSComponent(URI identifier) {
        super(identifier);
        if ("".equals(identifier.toString()) ||
                identifier.toString().charAt(identifier.toString().length() - 1) != '/') {
            throw new IllegalArgumentException("Invalid identifier uri for a component: " + identifier);
        }
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
