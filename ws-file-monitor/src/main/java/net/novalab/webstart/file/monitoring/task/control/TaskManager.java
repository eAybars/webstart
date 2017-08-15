package net.novalab.webstart.file.monitoring.task.control;

import net.novalab.webstart.file.monitoring.task.entity.Task;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class TaskManager {
    private static final long TRIGGER_DURATION = Long.parseLong(System.getProperty("UPDATE_TIMEOUT", "60000"));
    private static final Logger LOGGER = Logger.getLogger(TaskManager.class.getName());

    @Resource
    TimerService timerService;

    @Inject
    PathWatchService pathWatchService;

    private NavigableMap<Path, Task> tasks = new TreeMap<>();

    @Lock(LockType.WRITE)
    public void add(Task task) {
        Optional<Task> optional = findTask(task.getDomain());
        if (optional.isPresent()) {
            throw new IllegalArgumentException("A task already exists on domain " + optional.get().getDomain());
        }
        tasks.put(task.getDomain(), task);
        LOGGER.log(Level.INFO, "Registered task for execution: " + task);
        timerService.getTimers().forEach(Timer::cancel);
        timerService.createTimer(TRIGGER_DURATION, null);
        try {
            pathWatchService.register(task.getDomain());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to register path for listening: " + task.getDomain(), e);
        }
    }

    @Lock(LockType.READ)
    public Optional<Task> findTask(Path path) {
        NavigableMap<Path, Task> map = tasks.headMap(path, true);
        if (map.isEmpty() || !path.startsWith(map.lastKey())) {
            return Optional.empty();
        } else {
            return Optional.of(map.lastEntry().getValue());
        }
    }

    @Lock(LockType.READ)
    public Set<Task> findTasksUnder(Path path) {
        return tasks.tailMap(path, true)
                .values().stream()
                .filter(t -> t.getDomain().startsWith(path))
                .collect(Collectors.toSet());
    }

    @Lock(LockType.WRITE)
    public void remove(Task task) {
        if (tasks.remove(task.getDomain()) != null) {
            pathWatchService.unregisterAll(task.getDomain());
        }
    }

    @Lock(LockType.WRITE)
    public void removeAll(Set<Task> task) {
        task.stream().map(t -> tasks.remove(t.getDomain()))
                .filter(Objects::nonNull)
                .map(Task::getDomain)
                .forEach(pathWatchService::unregisterAll);
    }

    @Lock(LockType.WRITE)
    public void cancelAll() {
        timerService.getTimers().forEach(Timer::cancel);
        tasks.values().forEach(task -> pathWatchService.unregisterAll(task.getDomain()));
        tasks.clear();
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void execute() {
        Optional<Task> optionalTask;

        do{
            optionalTask = tasks.values().stream()
                    .filter(t -> t.timeSinceLastAction() > TRIGGER_DURATION)
                    .findFirst();
            optionalTask.ifPresent(this::execute);
        }while (optionalTask.isPresent());

        if (!tasks.isEmpty()) {
            timerService.createTimer(TRIGGER_DURATION, null);
        }
    }

    private void execute(Task task) {
        remove(task);
        task.run();
    }

}
