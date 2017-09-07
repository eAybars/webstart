package net.novalab.webstart.google.discovery.control;

import com.google.cloud.storage.Blob;
import net.novalab.webstart.google.artifact.entity.CloudStorageArtifact;
import net.novalab.webstart.google.artifact.entity.CloudStorageResource;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@ApplicationScoped
public class PdfArtifactCreator extends AbstractArtifactCreator {

    @Override
    public Stream<? extends CloudStorageArtifact> apply(URI uri) {
        return blobs(uri)
                .filter(((Predicate<Blob>) Blob::isDirectory).negate())
                .filter(b -> b.getName().endsWith(".pdf"))
                .sorted(Comparator.comparing(Blob::getName))
                .map(b -> new CloudStorageResource(toArtifactURI(b)));
    }
}
