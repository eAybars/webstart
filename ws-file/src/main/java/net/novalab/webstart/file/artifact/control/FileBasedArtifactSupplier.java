package net.novalab.webstart.file.artifact.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.discovery.control.ArtifactScanner;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.control.ArtifactSupplier;
import net.novalab.webstart.service.artifact.entity.Artifact;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class FileBasedArtifactSupplier implements ArtifactSupplier {
    private static final Logger LOGGER = Logger.getLogger(FileBasedArtifactSupplier.class.getName());

    @Inject
    Event<Artifact> artifactEvent;
    @Inject
    ArtifactScanner artifactScanner;


    private List<FileBasedArtifact> components;

    public FileBasedArtifactSupplier() {
        components = new CopyOnWriteArrayList<>();
    }

    @Override
    public Stream<? extends FileBasedArtifact> get() {
        return components.stream();
    }


    public Optional<FileBasedArtifact> findComponent(Path path) {
        return components.stream()
                .filter(c -> c.getIdentifierFile().toPath().equals(path))
                .findFirst();
    }

    public Path root() {
        return artifactScanner.getArtifactRoot().toPath();
    }

    @PostConstruct
    public void reloadAll() {
        Stream.of(artifactScanner.getArtifactRoot().listFiles(File::isDirectory))
                .map(File::toPath)
                .forEach(this::reloadComponents);
    }


    public void reloadComponents(Path path) {
        unloadComponents(path);
        loadComponents(path);
    }

    public void loadComponents(Path path) {
        List<? extends FileBasedArtifact> components = artifactScanner.apply(path.toFile());
        this.components.addAll(components);
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        components.forEach(loadEvent::fire);
    }

    public void unloadComponents(Path path) {
        Set<FileBasedArtifact> components = this.components.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toSet());
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        this.components.removeAll(components);
        components.forEach(unloadEvent::fire);
    }

    public void updateComponents(Path path) {
        Map<Path, FileBasedArtifact> existing = components.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));
        Map<Path, FileBasedArtifact> loaded = artifactScanner.apply(path.toFile())
                .stream()
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));

        //process updates
        Event<Artifact> updateEvent = artifactEvent.select(ArtifactEvent.Literal.UPDATED);
        this.components.replaceAll(c -> loaded.getOrDefault(c.getIdentifierFile().toPath(), c));
        loaded.entrySet().stream()
                .filter(e -> existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .forEach(updateEvent::fire);

        //process unloaded
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        Set<FileBasedArtifact> unloadedComponents = existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        components.removeAll(unloadedComponents);
        unloadedComponents.forEach(unloadEvent::fire);

        //process loaded
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        Set<FileBasedArtifact> loadedComponents = loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        components.addAll(loadedComponents);
        loadedComponents.forEach(loadEvent::fire);
    }
}
