package net.novalab.webstart.file.component.control;

import net.novalab.webstart.file.component.entity.FileBasedComponent;
import net.novalab.webstart.file.discovery.control.ComponentScanner;
import net.novalab.webstart.service.component.control.ComponentEvent;
import net.novalab.webstart.service.component.control.ComponentEventImpl;
import net.novalab.webstart.service.component.control.ComponentSupplier;
import net.novalab.webstart.service.component.entity.Component;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class FileBasedComponentSupplier implements ComponentSupplier {
    private static final Logger LOGGER = Logger.getLogger(FileBasedComponentSupplier.class.getName());

    @Inject
    Event<Component> componentEvent;
    @Inject
    ComponentScanner componentScanner;


    private List<FileBasedComponent> components;

    public FileBasedComponentSupplier() {
        components = new CopyOnWriteArrayList<>();
    }

    @Override
    public Stream<? extends FileBasedComponent> get() {
        return components.stream();
    }


    public Optional<FileBasedComponent> findComponent(Path path) {
        return components.stream()
                .filter(c -> c.getBaseDirectory().toPath().equals(path))
                .findFirst();
    }

    public Path root() {
        return componentScanner.getArtifactRoot().toPath();
    }

    @PostConstruct
    public void reloadAll() {
        Stream.of(componentScanner.getArtifactRoot().listFiles(File::isDirectory))
                .map(File::toPath)
                .forEach(this::reloadComponents);
    }


    public void reloadComponents(Path path) {
        unloadComponents(path);
        loadComponents(path);
    }

    public void loadComponents(Path path) {
        List<? extends FileBasedComponent> components = componentScanner.apply(path.toFile());
        this.components.addAll(components);
        Event<Component> loadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.LOADED));
        components.forEach(loadEvent::fire);
    }

    public void unloadComponents(Path path) {
        Set<FileBasedComponent> components = this.components.stream()
                .filter(c -> c.getBaseDirectory().toPath().startsWith(path))
                .collect(Collectors.toSet());
        Event<Component> unloadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UNLOADED));
        this.components.removeAll(components);
        components.forEach(unloadEvent::fire);
    }

    public void updateComponents(Path path) {
        Map<Path, FileBasedComponent> existing = components.stream()
                .filter(c -> c.getBaseDirectory().toPath().startsWith(path))
                .collect(Collectors.toMap(c -> c.getBaseDirectory().toPath(), Function.identity()));
        Map<Path, FileBasedComponent> loaded = componentScanner.apply(path.toFile())
                .stream()
                .collect(Collectors.toMap(c -> c.getBaseDirectory().toPath(), Function.identity()));

        //process updates
        Event<Component> updateEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UPDATED));
        this.components.replaceAll(c -> loaded.getOrDefault(c.getBaseDirectory().toPath(), c));
        loaded.entrySet().stream()
                .filter(e -> existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .forEach(updateEvent::fire);

        //process unloaded
        Event<Component> unloadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.UNLOADED));
        Set<FileBasedComponent> unloadedComponents = existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        components.removeAll(unloadedComponents);
        unloadedComponents.forEach(unloadEvent::fire);

        //process loaded
        Event<Component> loadEvent = componentEvent.select(new ComponentEventImpl(ComponentEvent.Type.LOADED));
        Set<FileBasedComponent> loadedComponents = loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        components.addAll(loadedComponents);
        loadedComponents.forEach(loadEvent::fire);
    }
}
