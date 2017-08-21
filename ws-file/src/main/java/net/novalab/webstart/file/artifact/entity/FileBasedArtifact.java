package net.novalab.webstart.file.artifact.entity;

import net.novalab.webstart.service.artifact.entity.AbstractArtifact;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * Created by ertunc on 20/06/17.
 */
public abstract class FileBasedArtifact extends AbstractArtifact {
    private File identifierFile;

    public FileBasedArtifact(URI identifier, File identifierFile) {
        super(identifier);
        this.identifierFile = Objects.requireNonNull(identifierFile);
        this.setTitle(identifierFile.getName());
    }

    public File getIdentifierFile() {
        return identifierFile;
    }

    protected URL toResourceURL(File file) {
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }


}
