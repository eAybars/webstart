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
public class ExecutableFileUpdatedEventListener implements PathWatchService.EventListener {
    @Inject
    TaskManager taskManager;
    @Inject
    FileBasedArtifactSupplier fileBasedComponentSupplier;


    @Override
    public void accept(PathWatchServiceEvent e) {
        taskManager.add(new Task(e.getPath(), "Update component(s) for " + e.getPath().getParent(), () -> {
            fileBasedComponentSupplier.update(e.getPath().getParent());
        }));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return !Files.isDirectory(e.getPath()) &&
                e.getPath().getFileName().toString().endsWith(".jnlp") &&
                !taskManager.findTask(e.getPath()).isPresent() &&
                fileBasedComponentSupplier.findComponent(e.getPath().getParent()).isPresent();
    }
}
