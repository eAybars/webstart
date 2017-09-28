package com.eaybars.webstart.file.schedule.control;

import com.eaybars.webstart.file.action.entity.Action;
import com.eaybars.webstart.file.watch.control.PathWatchService;
import com.eaybars.webstart.file.watch.entity.PathWatchServiceEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ActionTimeUpdatingEventListener implements PathWatchService.EventListener {
    private static Logger LOGGER = Logger.getLogger(ActionTimeUpdatingEventListener.class.getName());

    @Inject
    ActionScheduler actionScheduler;

    @Override
    public void accept(PathWatchServiceEvent event) {
        Optional<Action> optionalTask = actionScheduler.findParentAction(event.getPath());
        optionalTask.ifPresent(Action::updateLastActionTimeToNow);
        if (optionalTask.isPresent() && Files.isDirectory(event.getPath())) {
            try {
                event.getService().register(event.getPath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to register path for watching: " + event.getPath(), e);
            }
        }

    }
}
