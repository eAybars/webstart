package net.novalab.webstart.file.monitoring.component.control;

import net.novalab.webstart.file.discovery.control.ComponentScanner;
import net.novalab.webstart.file.discovery.entity.FileBasedComponent;
import net.novalab.webstart.file.monitoring.task.control.TaskManager;
import net.novalab.webstart.file.monitoring.watch.control.PathWatchService;
import net.novalab.webstart.service.application.controller.ComponentEvent;
import net.novalab.webstart.service.application.controller.ComponentEventImpl;
import net.novalab.webstart.service.application.controller.ComponentSupplier;
import net.novalab.webstart.service.application.entity.Component;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Startup
@ConcurrencyManagement
@LocalBean
@Local
public class FileBasedComponentSupplier implements ComponentSupplier {
    private static final Logger LOGGER = Logger.getLogger(FileBasedComponentSupplier.class.getName());

    @Inject
    Event<Component> componentEvent;
    @Inject
    ComponentScanner componentScanner;
    @Inject
    PathWatchService pathWatchService;
    @Inject
    TaskManager taskManager;


    private List<FileBasedComponent> components;

    public FileBasedComponentSupplier() {
        components = new LinkedList<>();
    }

    @Override
    @Lock(LockType.READ)
    public Stream<? extends FileBasedComponent> get() {
        return components.stream();
    }


    @Lock(LockType.READ)
    public Optional<FileBasedComponent> findComponent(Path path) {
        return components.stream()
                .filter(c -> c.getBaseDirectory().toPath().equals(path))
                .findFirst();
    }

    public Path root() {
        return componentScanner.getArtifactRoot().toPath();
    }

    @Lock(LockType.WRITE)
    @PostConstruct
    public void reloadAll() {
        pathWatchService.unregisterAll();
        taskManager.cancelAll();

        reloadComponents(componentScanner.getArtifactRoot().toPath());
    }


    @Lock(LockType.WRITE)
    public void reloadComponents(Path path) {
        unloadComponents(path);
        loadComponents(path);
    }

    @Lock(LockType.WRITE)
    public void loadComponents(Path path) {
        components.addAll(componentScanner.andThen(components -> {
            Event<Component> loadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.LOADED));
            for (FileBasedComponent component : components) {
                try {
                    pathWatchService.register(component.getBaseDirectory().toPath());
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Cannot register folder listening for component " + component.getIdentifier(), e);
                }
            }
            components.forEach(loadEvent::fire);
            return components;
        }).apply(path.toFile()));
    }

    @Lock(LockType.WRITE)
    public void unloadComponents(Path path) {
        Event<Component> unloadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UNLOADED));
        for (Iterator<FileBasedComponent> iterator = components.iterator(); iterator.hasNext();) {
            FileBasedComponent component = iterator.next();
            if (component.getBaseDirectory().toPath().startsWith(path)) {
                iterator.remove();
                unloadEvent.fire(component);
            }
        }
    }

    @Lock(LockType.WRITE)
    public void updateComponents(Path path) {
        Map<Path, FileBasedComponent> existing = components.stream()
                .filter(c -> c.getBaseDirectory().toPath().startsWith(path))
                .collect(Collectors.toMap(c -> c.getBaseDirectory().toPath(), Function.identity()));
        Map<Path, ? extends FileBasedComponent> loaded = componentScanner.apply(path.toFile())
                .stream()
                .collect(Collectors.toMap(c -> c.getBaseDirectory().toPath(), Function.identity()));

        //process updates
        Event<Component> updateEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UPDATED));
        for (ListIterator<FileBasedComponent> iterator = components.listIterator(); iterator.hasNext();) {
            FileBasedComponent old = iterator.next();
            FileBasedComponent newComp = loaded.get(old.getBaseDirectory().toPath());
            if (newComp != null) {
                iterator.set(newComp);
                updateEvent.fire(newComp);
            }
        }

        //process unloaded
        Event<Component> unloadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UNLOADED));
        existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .forEach(unloadEvent::fire);

        //process loaded
        Event<Component> loadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.LOADED));
        loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .forEach(loadEvent::fire);
    }
}
