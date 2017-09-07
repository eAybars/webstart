package net.novalab.webstart.google.discovery.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import net.novalab.webstart.google.artifact.entity.CloudStorageArtifact;
import net.novalab.webstart.google.artifact.entity.CloudStorageExecutable;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ExecutableArtifactCreator extends AbstractArtifactCreator {
    @Override
    public Stream<? extends CloudStorageArtifact> apply(URI uri) {
        List<Blob> blobs = blobs(uri).collect(Collectors.toList());
        return blobs.stream()
                .filter(((Predicate<Blob>) Blob::isDirectory).negate())
                .filter(b -> b.getName().endsWith(".jnlp"))
                .sorted(Comparator.comparing(BlobInfo::getName))
                .map(b -> createExecutable(uri, b))
                .map(exe -> addResources(blobs, exe))
                .limit(1);
    }

    private CloudStorageExecutable createExecutable(URI uri, Blob b) {
        CloudStorageExecutable executable = new CloudStorageExecutable(uri, toArtifactURI(b));
        executable.setDateModified(new Date(b.getUpdateTime()));
        return executable;
    }

    private CloudStorageExecutable addResources(List<Blob> blobs, CloudStorageExecutable executable) {
        blobs.stream()
                .filter(((Predicate<Blob>) Blob::isDirectory).negate())
                .map(BlobInfo::getName)
                .map("/"::concat)
                .collect(Collectors.toCollection(executable::getResources));

        blobs.stream()
                .filter(Blob::isDirectory)
                .map(Blob::getName)
                .map(URI::create)
                .map(this::blobs)
                .map(s -> s.collect(Collectors.toList()))
                .forEach(newBlobs -> addResources(newBlobs, executable));
        return executable;
    }
}
