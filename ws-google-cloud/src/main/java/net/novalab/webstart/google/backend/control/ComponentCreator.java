package net.novalab.webstart.google.backend.control;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import net.novalab.webstart.google.artifact.entity.GCSComponent;
import net.novalab.webstart.service.artifact.entity.Artifact;

import java.net.URI;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ComponentCreator extends ArtifactCreator {
    public ComponentCreator(GCSBackend backend) {
        super(backend);
    }

    @Override
    public Artifact apply(URI target) {
        GCSComponent component = new GCSComponent(target);

        getBackend().blobContents(target, Storage.BlobListOption.fields(
                Storage.BlobField.KIND))
                .filter(((Predicate<Blob>) Blob::isDirectory).negate())
                .map(BlobInfo::getName)
                .map("/"::concat)
                .collect(Collectors.toCollection(component::getResources));
        return component;
    }
}
