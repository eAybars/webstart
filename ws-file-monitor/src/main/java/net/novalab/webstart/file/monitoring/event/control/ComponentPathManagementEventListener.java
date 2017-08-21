package net.novalab.webstart.file.monitoring.event.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ComponentPathManagementEventListener {

    private static final Logger LOGGER = Logger.getLogger(ComponentPathManagementEventListener.class.getName());

    @Inject
    PathWatchService pathWatchService;

    public void onComponentLoad(@Observes(notifyObserver = Reception.ALWAYS)
                                @ArtifactEvent(ArtifactEvent.Type.LOADED)
                                        FileBasedArtifact c) {
        try {
            pathWatchService.register(c.getIdentifierFile().toPath());
            LOGGER.log(Level.INFO, "Registered path for listening: " + c.getIdentifierFile().toPath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot register path for listening: " + c.getIdentifierFile().toPath(), e);
        }
    }

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                @ArtifactEvent(ArtifactEvent.Type.UNLOADED)
                                          FileBasedArtifact c) {
        try {
            pathWatchService.unregister(c.getIdentifierFile().toPath());
            LOGGER.log(Level.INFO, "Unregistered path for listening: " + c.getIdentifierFile().toPath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occured while unregistering path for listening: " + c.getIdentifierFile().toPath(), e);
        }
    }
}
