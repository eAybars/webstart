package com.eaybars.webstart.file.schedule.control;

import com.eaybars.webstart.file.action.entity.Action;
import com.eaybars.webstart.file.backend.control.FileBackend;
import com.eaybars.webstart.file.watch.control.PathWatchService;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;
import com.eaybars.webstart.service.backend.control.BackendArtifacts;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class ActionScheduler {
    static final long TRIGGER_DURATION = Long.parseLong(System.getProperty("UPDATE_TIMEOUT", "60000"));
    private static final Logger LOGGER = Logger.getLogger(ActionScheduler.class.getName());

    private NavigableMap<Path, Action> actions = new TreeMap<>();

    @Resource
    TimerService timerService;

    @Inject
    PathWatchService pathWatchService;

    @Inject
    FileBackend fileBackend;

    @Inject
    BackendArtifacts backendArtifacts;

    @Lock(LockType.WRITE)
    public boolean add(Action action) {
        if (isActionRequired(action)) {
            findActionsUnder(action.getDomain()).forEach(this::remove);
            actions.put(action.getDomain(), action);
            timerService.getTimers().forEach(Timer::cancel);
            timerService.createTimer(TRIGGER_DURATION, null);
            LOGGER.log(Level.INFO, "Scheduled action: " + action);

            if (Files.isDirectory(action.getDomain())) {
                if (action.getType().equals(ArtifactEvent.Type.UNLOAD)) {
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
            }

            return true;
        }
        return false;
    }

    /**
     * Operate according to the following matrix
     * <p>
     *                  Self    Sub
     * ------------------------------
     * Load, Load	 |	x   |	x
     * Load, UnLoad	 |	OK	|   x
     * Load Update	 |	x	|   x
     * --------------+------+---------
     * UnLoad UnLoad |	x	|   Na
     * UnLoad Load	 |	OK	|   Na
     * UnLoad Update |	Na  |	Na
     * --------------+------+---------
     * Update Update |	x   |	x
     * Update Load	 |	Na	|   x
     * Update Unload |	x   |	x
     * --------------+------+---------
     * <p>
     * * @param action
     * * @return
     */
    private boolean isActionRequired(Action action) {
        Optional<Action> actionOptional = findParentAction(action.getDomain());
        return actionOptional
                .map(parent -> parent.getDomain().equals(action.getDomain()) &&
                        !ArtifactEvent.Type.UPDATE.equals(parent.getType()) &&
                        (ArtifactEvent.Type.LOAD.equals(parent.getType())
                                ? ArtifactEvent.Type.UNLOAD.equals(action.getType())
                                : ArtifactEvent.Type.LOAD.equals(action.getType())))
                .orElse(true);
    }

    @Lock(LockType.READ)
    public Action getAction(Path domain) {
        return actions.get(domain);
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
        if (actions.remove(action.getDomain()) != null && Files.isDirectory(action.getDomain())) {
            pathWatchService.unregisterAll(action.getDomain());
        }
    }

    @Lock(LockType.WRITE)
    public void removeAll(Set<Action> actions) {
        actions.forEach(this::remove);
    }

    @Lock(LockType.WRITE)
    public void cancelAll() {
        timerService.getTimers().forEach(Timer::cancel);
        actions.values().stream()
                .map(Action::getDomain)
                .filter(Files::isDirectory)
                .forEach(pathWatchService::unregisterAll);
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
        URI uri = fileBackend.toURI(action.getDomain().toFile());
        switch (action.getType()) {
            case LOAD:
                backendArtifacts.load(uri);
                break;
            case UNLOAD:
                backendArtifacts.unload(uri);
                break;
            case UPDATE:
                backendArtifacts.update(uri);
                break;
        }
    }
}
