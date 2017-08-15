package net.novalab.webstart.file.monitoring.event.control;

import net.novalab.webstart.file.monitoring.component.control.FileBasedComponentSupplier;
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
public class ComponentCreatedEventListener implements PathWatchService.EventListener {
    @Inject
    TaskManager taskManager;
    @Inject
    FileBasedComponentSupplier fileBasedComponentSupplier;

    @Override
    public void accept(PathWatchServiceEvent e) {
        TreeSet<Path> parentComponents = fileBasedComponentSupplier.get()
                .map(c -> c.getBaseDirectory().toPath())
                .filter(cPath -> e.getPath().startsWith(cPath))
                .collect(Collectors.toCollection(TreeSet::new));

        Path parent = parentComponents.isEmpty() ? fileBasedComponentSupplier.root() : parentComponents.last();
        Path pathToScan = parent.subpath(0, parent.getNameCount() + 1);

        taskManager.add(new Task(e.getPath(), "Create components for " + pathToScan, () -> {
            fileBasedComponentSupplier.loadComponents(pathToScan);
        }));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isCreate() && Files.isDirectory(e.getPath()) &&
                !taskManager.findTask(e.getPath()).isPresent();
    }
}
