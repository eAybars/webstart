package net.novalab.webstart.file.monitoring.event.control;

import net.novalab.webstart.file.artifact.control.FileBasedArtifactSupplier;
import net.novalab.webstart.file.monitoring.task.control.TaskManager;
import net.novalab.webstart.file.monitoring.task.entity.Task;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.file.monitoring.watch.entity.PathWatchServiceEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;

@ApplicationScoped
public class ArtifactRemovedEventListener implements PathWatchService.EventListener{
    @Inject
    TaskManager taskManager;
    @Inject
    FileBasedArtifactSupplier fileBasedComponentSupplier;

    @Override
    public void accept(PathWatchServiceEvent e) {
        taskManager.add(new Task(e.getPath(), "Remove components for " + e.getPath(), () -> {
            fileBasedComponentSupplier.unload(e.getPath());
        }));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isDelete() && Files.isDirectory(e.getPath()) &&
                !taskManager.findTask(e.getPath()).isPresent();
    }

}