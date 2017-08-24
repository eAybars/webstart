package net.novalab.webstart.file.monitoring.event.control;

import net.novalab.webstart.file.artifact.control.FileBasedArtifactSupplier;
import net.novalab.webstart.file.monitoring.task.control.TaskManager;
import net.novalab.webstart.file.monitoring.task.entity.Task;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.file.monitoring.watch.entity.PathWatchServiceEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArtifactCreatedEventListener implements PathWatchService.EventListener {
    @Inject
    TaskManager taskManager;
    @Inject
    FileBasedArtifactSupplier fileBasedComponentSupplier;

    @Override
    public void accept(PathWatchServiceEvent e) {
        TreeSet<Path> parentComponents = fileBasedComponentSupplier.get()
                .map(c -> c.getIdentifierFile().toPath())
                .filter(cPath -> e.getPath().startsWith(cPath))
                .collect(Collectors.toCollection(TreeSet::new));

        Path parent = parentComponents.isEmpty() ? fileBasedComponentSupplier.root() : parentComponents.last();
        Path pathToScan = parent.subpath(0, parent.getNameCount() + 1);

        taskManager.add(new Task(e.getPath(), "Create components for " + pathToScan, () -> {
            fileBasedComponentSupplier.load(pathToScan);
        }));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isCreate() && Files.isDirectory(e.getPath()) &&
                !taskManager.findTask(e.getPath()).isPresent();
    }
}
