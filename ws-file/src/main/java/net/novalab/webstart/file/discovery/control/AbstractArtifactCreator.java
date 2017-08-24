package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.discovery.entity.ArtifactRoot;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;

public abstract class AbstractArtifactCreator implements ArtifactCreator {
    @Inject
    @ArtifactRoot
    File artifactRoot;

    protected URI toIdentifier(File identifierFile) {
        return URI.create("/" + artifactRoot.toURI().relativize(identifierFile.toURI()));
    }

    public File getArtifactRoot() {
        return artifactRoot;
    }
}
