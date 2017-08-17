package net.novalab.webstart.file.monitoring.event.control;

import net.novalab.webstart.file.component.entity.FileBasedComponent;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.service.component.control.ComponentEvent;

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
                                @ComponentEvent(ComponentEvent.Type.LOADED)
                                        FileBasedComponent c) {
        try {
            pathWatchService.register(c.getBaseDirectory().toPath());
            LOGGER.log(Level.INFO, "Registered path for listening: " + c.getBaseDirectory().toPath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot register path for listening: " + c.getBaseDirectory().toPath(), e);
        }
    }

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                @ComponentEvent(ComponentEvent.Type.UNLOADED)
                                        FileBasedComponent c) {
        try {
            pathWatchService.unregister(c.getBaseDirectory().toPath());
            LOGGER.log(Level.INFO, "Unregistered path for listening: " + c.getBaseDirectory().toPath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occured while unregistering path for listening: " + c.getBaseDirectory().toPath(), e);
        }
    }
}
