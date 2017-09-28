package com.eaybars.webstart.google.backend.control;

import com.eaybars.webstart.google.artifact.entity.GCSExecutable;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.eaybars.webstart.service.artifact.entity.Artifact;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ExecutableCreator extends ArtifactCreator {
    private static final Logger LOGGER = Logger.getLogger(ExecutableCreator.class.getName());

    public ExecutableCreator(GCSBackend backend) {
        super(backend);
    }

    @Override
    public Artifact apply(URI target) {
        GCSExecutable executable = null;
        try {
            executable = new GCSExecutable(target);

            List<Blob> blobs = getBackend().blobContents(URI.create(executable.getIdentifier().toString().substring(1)),
                    Storage.BlobListOption.fields(
                            Storage.BlobField.UPDATED,
                            Storage.BlobField.KIND))
                    .filter(((Predicate<Blob>) Blob::isDirectory).negate())
                    .collect(Collectors.toList());

            blobs.stream()
                    .map(BlobInfo::getName)
                    .map("/"::concat)
                    .collect(Collectors.toCollection(executable::getResources));

            blobs.stream().filter(b -> b.getName().equals(target.toString()))
                    .findFirst()
                    .map(BlobInfo::getUpdateTime)
                    .map(Date::new)
                    .ifPresent(executable::setDateModified);

        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Cannot create executable artifact for " + target, e);
        }
        return executable;
    }
}
