package net.novalab.webstart.google.artifact.entity;

import net.novalab.webstart.service.artifact.entity.AbstractArtifact;
import net.novalab.webstart.service.uri.control.protocol.gcs.Handler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class CloudStorageArtifact extends AbstractArtifact {

    public CloudStorageArtifact(URI identifier) {
        super(identifier);
    }

    protected URL toURL(String path) throws MalformedURLException {
        return new URL(null,net.novalab.webstart.service.uri.control.protocol.gcs.Handler.PROTOCOL_NAME + "://" + path, Handler.INSTANCE);
    }

}
