package com.eaybars.webstart.file.watch.control;

import com.eaybars.webstart.file.backend.control.FileBackend;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class PathWatchingManagementEventListener {

    private static final Logger LOGGER = Logger.getLogger(PathWatchingManagementEventListener.class.getName());

    @Inject
    PathWatchService pathWatchService;
    @Inject
    FileBackend fileBackend;

    public void onArtifactLoad(@Observes(notifyObserver = Reception.ALWAYS)
                                @ArtifactEvent(ArtifactEvent.Type.LOAD)
                                        Artifact a) {
        try {
            pathWatchService.register(fileBackend.toFile(a.getIdentifier()).toPath());
            LOGGER.log(Level.INFO, "Registered path for listening: " + a.getIdentifier());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot register path for listening: " + a.getIdentifier(), e);
        }
    }

    public void onArtifactUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                @ArtifactEvent(ArtifactEvent.Type.UNLOAD)
                                         Artifact a) {
        try {
            pathWatchService.unregister(fileBackend.toFile(a.getIdentifier()).toPath());
            LOGGER.log(Level.INFO, "Unregistered path for listening: " + a.getIdentifier());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occured while unregistering path for listening: " + a.getIdentifier(), e);
        }
    }
}
