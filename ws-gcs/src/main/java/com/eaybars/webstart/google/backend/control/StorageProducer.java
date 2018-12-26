package com.eaybars.webstart.google.backend.control;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.Optional;

/**
 * To authenticate with google cloud storage api do one of the followings:
 * <ul>
 *     <li>Set environment variable GOOGLE_APPLICATION_CREDENTIALS so that it points to service account file</li>
 *     <li>Run this service in one of the following Google cloud services: Compute Engine, Container Engine or App Engine,</li>
 * </ul>.
 */
@ApplicationScoped
public class StorageProducer {

    private Storage storage;
    private Bucket bucket;

    public StorageProducer() {
        storage = StorageOptions.getDefaultInstance().getService();
        String bucketName = Optional.ofNullable(System.getProperty("WEB_START_ARTIFACT_ROOT",
                System.getenv("WEB_START_ARTIFACT_ROOT"))).orElse("/webstart");
        bucket = storage.get(bucketName);
        if (bucket == null) {
            bucket = storage.create(BucketInfo.of(bucketName));
        }
    }

    @Produces
    public Storage getStorage() {
        return storage;
    }

    @Produces
    public Bucket getBucket() {
        return bucket;
    }

}
