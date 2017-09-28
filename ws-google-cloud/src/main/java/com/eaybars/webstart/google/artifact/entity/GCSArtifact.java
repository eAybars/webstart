package com.eaybars.webstart.google.artifact.entity;

import com.eaybars.webstart.service.artifact.entity.AbstractArtifact;
import com.eaybars.webstart.service.uri.control.protocol.gcs.Handler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class GCSArtifact extends AbstractArtifact {

    public GCSArtifact(URI identifier) {
        super(toIdentifierURI(identifier));
    }

    protected URL toURL(String path) throws MalformedURLException {
        return new URL(null, Handler.PROTOCOL_NAME + "://" + path, Handler.INSTANCE);
    }

    public static URI toIdentifierURI(URI uri) {
        return uri.toString().charAt(0) == '/' ? uri : URI.create("/" + uri.toString());
    }
}
