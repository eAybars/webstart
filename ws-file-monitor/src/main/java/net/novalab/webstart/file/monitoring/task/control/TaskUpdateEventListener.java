package net.novalab.webstart.file.monitoring.task.control;

import net.novalab.webstart.file.monitoring.task.entity.Task;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.file.monitoring.watch.entity.PathWatchServiceEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class TaskUpdateEventListener implements PathWatchService.EventListener {
    private static Logger LOGGER = Logger.getLogger(TaskUpdateEventListener.class.getName());

    @Inject
    TaskManager taskManager;

    @Override
    public void accept(PathWatchServiceEvent event) {
        Optional<Task> optionalTask = taskManager.findTask(event.getPath());
        optionalTask.ifPresent(Task::updateLastActionTimeToNow);
        if (optionalTask.isPresent() && Files.isDirectory(event.getPath())) {
            try {
                event.getService().register(event.getPath());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to register path for watching: " + event.getPath(), e);
            }
        }
    }
}
