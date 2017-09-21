package net.novalab.webstart.file.schedule.control;

import net.novalab.webstart.file.backend.control.FileBackend;
import net.novalab.webstart.file.action.entity.Action;
import net.novalab.webstart.file.watch.control.PathWatchService;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.discovery.control.BackendArtifactSupplier;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ActionScheduler {
    private static final long TRIGGER_DURATION = Long.parseLong(System.getProperty("UPDATE_TIMEOUT", "60000"));
    private static final Logger LOGGER = Logger.getLogger(ActionScheduler.class.getName());

    private NavigableMap<Path, Action> actions = new TreeMap<>();

    @Resource
    TimerService timerService;

    @Inject
    PathWatchService pathWatchService;

    @Inject
    FileBackend fileBackend;

    @Inject
    BackendArtifactSupplier artifactSupplier;

    @Lock(LockType.WRITE)
    public boolean add(Action action) {
        Optional<Action> actionOptional = findParentAction(action.getDomain());
        if (!actionOptional.isPresent() || isSubActionNeeded(actionOptional.get(), action)) {
            findActionsUnder(action.getDomain()).stream()
                    .filter(subAction -> !isSubActionNeeded(action, subAction))
                    .forEach(this::remove);
            actions.put(action.getDomain(), action);
            LOGGER.log(Level.INFO, "Scheduled action: " + action);
            timerService.getTimers().forEach(Timer::cancel);
            timerService.createTimer(TRIGGER_DURATION, null);
            if (action.getType().equals(ArtifactEvent.Type.UPDATE)) {
                try {
                    pathWatchService.unregisterAll(action.getDomain());
                } catch (Exception e) {
                    //not important
                }
            } else {
                try {
                    pathWatchService.register(action.getDomain());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Unable to register path for listening: " + action.getDomain(), e);
                }
            }

            return true;
        }
        return false;
    }

    private boolean isSubActionNeeded(Action parent, Action child) {
        return parent.getType().compareTo(child.getType()) < 0;
    }

    @Lock(LockType.READ)
    public Optional<Action> findParentAction(Path path) {
        NavigableMap<Path, Action> map = actions.headMap(path, true);
        if (map.isEmpty() || !path.startsWith(map.lastKey())) {
            return Optional.empty();
        } else {
            return Optional.of(map.lastEntry().getValue());
        }
    }

    @Lock(LockType.READ)
    public Set<Action> findActionsUnder(Path path) {
        return actions.tailMap(path, true)
                .values().stream()
                .filter(t -> t.getDomain().startsWith(path))
                .collect(Collectors.toSet());
    }

    @Lock(LockType.WRITE)
    public void remove(Action action) {
        if (actions.remove(action.getDomain()) != null) {
            pathWatchService.unregisterAll(action.getDomain());
        }
    }

    @Lock(LockType.WRITE)
    public void removeAll(Set<Action> actions) {
        actions.stream().map(t -> this.actions.remove(t.getDomain()))
                .filter(Objects::nonNull)
                .map(Action::getDomain)
                .forEach(pathWatchService::unregisterAll);
    }

    @Lock(LockType.WRITE)
    public void cancelAll() {
        timerService.getTimers().forEach(Timer::cancel);
        actions.values().forEach(task -> pathWatchService.unregisterAll(task.getDomain()));
        actions.clear();
    }

    @Timeout
    @Lock(LockType.WRITE)
    public void execute() {
        Optional<Action> optionalAction;

        do {
            optionalAction = actions.values().stream()
                    .filter(t -> t.timeSinceLastAction() > TRIGGER_DURATION)
                    .findFirst();
            optionalAction.ifPresent(this::execute);
        } while (optionalAction.isPresent());

        if (!actions.isEmpty()) {
            timerService.createTimer(TRIGGER_DURATION, null);
        }
    }

    private void execute(Action action) {
        remove(action);
        URI uri = fileBackend.toBackendURI(action.getDomain().toFile());
        switch (action.getType()) {
            case LOAD:
                artifactSupplier.load(uri);
                break;
            case UNLOAD:
                artifactSupplier.unload(uri);
                break;
            case UPDATE:
                artifactSupplier.update(uri);
                break;
        }
    }
}