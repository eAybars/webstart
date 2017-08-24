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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
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


    private List<FileBasedArtifact> artifacts;

    public FileBasedArtifactSupplier() {
        artifacts = new CopyOnWriteArrayList<>();
    }

    @Override
    public Stream<? extends FileBasedArtifact> get() {
        return artifacts.stream();
    }


    public Optional<FileBasedArtifact> findComponent(Path path) {
        return artifacts.stream()
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
                .forEach(this::reload);
    }


    public void reload(Path path) {
        unload(path);
        load(path);
    }

    public void load(Path path) {
        List<? extends FileBasedArtifact> artifacts = artifactScanner.apply(path.toFile())
                .filter(((Predicate<FileBasedArtifact>)this.artifacts::contains).negate())
                .collect(Collectors.toList());
        this.artifacts.addAll(artifacts);
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
        artifacts.forEach(loadEvent::fire);
    }

    public void unload(Path path) {
        Set<FileBasedArtifact> artifacts = this.artifacts.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toSet());
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        this.artifacts.removeAll(artifacts);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));
        artifacts.forEach(unloadEvent::fire);
    }

    public void update(Path path) {
        Map<Path, FileBasedArtifact> existing = artifacts.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));
        Map<Path, FileBasedArtifact> loaded = artifactScanner.apply(path.toFile())
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));

        //process updates
        Event<Artifact> updateEvent = artifactEvent.select(ArtifactEvent.Literal.UPDATED);
        this.artifacts.replaceAll(c -> loaded.getOrDefault(c.getIdentifierFile().toPath(), c));
        loaded.entrySet().stream()
                .filter(e -> existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .forEach(((Consumer<Artifact>)updateEvent::fire)
                        .andThen(a -> LOGGER.log(Level.INFO, "Updated artifact " + a)));

        //process unloaded
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        Set<FileBasedArtifact> unloadedComponents = existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        artifacts.removeAll(unloadedComponents);
        unloadedComponents.forEach(unloadEvent::fire);
        unloadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));

        //process loaded
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        Set<FileBasedArtifact> loadedComponents = loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        artifacts.addAll(loadedComponents);
        loadedComponents.forEach(loadEvent::fire);
        loadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
    }
}
