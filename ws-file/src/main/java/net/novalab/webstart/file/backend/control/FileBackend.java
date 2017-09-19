package net.novalab.webstart.file.backend.control;

import net.novalab.webstart.file.artifact.entity.FileBasedComponent;
import net.novalab.webstart.file.artifact.entity.FileBasedExecutable;
import net.novalab.webstart.file.artifact.entity.FileBasedResource;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.Component;
import net.novalab.webstart.service.artifact.entity.Executable;
import net.novalab.webstart.service.artifact.entity.Resource;
import net.novalab.webstart.service.backend.control.Backend;
import net.novalab.webstart.service.backend.control.Storage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Stream;

@ApplicationScoped
public class FileBackend implements Backend {
    public static final URI NAME = URI.create("/local/");

    @Inject
    @ArtifactRoot
    File root;

    @Inject
    FileStorage fileStorage;

    public URI toBackendURI(File file) {
        return URI.create(NAME.toString() + "/" + root.toURI().relativize(file.toURI()));
    }

    public URI toArtifactId(File file) {
        return URI.create("/" + root.toURI().relativize(file.toURI()));
    }

    public File toFile(URI uri) {
        return new File(root, getName().relativize(uri).toString());
    }



    @Override
    public URI getName() {
        return NAME;
    }

    @Override
    public Stream<URI> contents(URI parent) {
        File file = new File(root, parent.toString());
        if (file.isDirectory()) {
            return Stream.of(file.listFiles())
                    .map(f -> root.toURI().relativize(f.toURI()));
        }
        return Stream.empty();
    }

    @Override
    public <T extends Artifact> T createArtifact(Class<T> type, URI target) {
        File identifierFile = new File(root, target.toString());
        if (Component.class.equals(type)) {
            return (T) new FileBasedComponent(toArtifactId(identifierFile), identifierFile);
        } else if (Executable.class.equals(type)) {
            return (T) new FileBasedExecutable(toArtifactId(identifierFile.getParentFile()), identifierFile);
        } else if (Resource.class.equals(type)) {
            return (T) new FileBasedResource(toArtifactId(identifierFile), identifierFile);
        } else {
            return null;
        }
    }

    @Override
    public Optional<Storage> getStorage() {
        return Optional.of(fileStorage);
    }
}
