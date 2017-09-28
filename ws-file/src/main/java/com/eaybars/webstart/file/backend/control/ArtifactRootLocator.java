package com.eaybars.webstart.file.backend.control;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.io.File;
import java.util.Optional;

/**
 * Created by ertunc on 20/06/17.
 */
@ApplicationScoped
public class ArtifactRootLocator {
    private File artifactRoot;

    @PostConstruct
    public void initFromConfiguration() {
        artifactRoot = new File(Optional.ofNullable(System.getProperty("WEB_START_ARTIFACT_ROOT",
                System.getenv("WEB_START_ARTIFACT_ROOT"))).orElse("/webstart"));
    }

    @Produces
    @ArtifactRoot
    public File getArtifactRoot() {
        return artifactRoot;
    }
}
