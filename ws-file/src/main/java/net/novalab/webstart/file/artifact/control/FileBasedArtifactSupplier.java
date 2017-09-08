package net.novalab.webstart.file.artifact.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.discovery.control.ArtifactScanner;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.control.ArtifactSupplier;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.ArtifactEventSummary;

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

    //post construct requires void return
    @PostConstruct
    public void init() {
        reloadAll();
    }

    public ArtifactEventSummary reloadAll() {
        return Stream.of(artifactScanner.getArtifactRoot().listFiles(File::isDirectory))
                .map(File::toPath)
                .map(this::reload)
                .reduce(ArtifactEventSummary::merge)
                .orElseGet(ArtifactEventSummary::new);
    }


    public ArtifactEventSummary reload(Path path) {
        return unload(path).merge(load(path));
    }

    public ArtifactEventSummary load(Path path) {
        List<? extends FileBasedArtifact> artifacts = artifactScanner.apply(path.toFile())
                .filter(((Predicate<FileBasedArtifact>) this.artifacts::contains).negate())
                .collect(Collectors.toList());
        this.artifacts.addAll(artifacts);
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
        artifacts.forEach(loadEvent::fire);
        ArtifactEventSummary summary = new ArtifactEventSummary();
        summary.setLoadedArtifacts(artifacts);
        return summary;
    }

    public ArtifactEventSummary unload(Path path) {
        Set<FileBasedArtifact> artifacts = this.artifacts.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toSet());
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        this.artifacts.removeAll(artifacts);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));
        artifacts.forEach(unloadEvent::fire);
        ArtifactEventSummary summary = new ArtifactEventSummary();
        summary.setUnloadedArtifacts(artifacts);
        return summary;
    }

    public ArtifactEventSummary update(Path path) {
        ArtifactEventSummary summary = new ArtifactEventSummary();

        Map<Path, FileBasedArtifact> existing = artifacts.stream()
                .filter(c -> c.getIdentifierFile().toPath().startsWith(path))
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));
        Map<Path, FileBasedArtifact> loaded = artifactScanner.apply(path.toFile())
                .collect(Collectors.toMap(c -> c.getIdentifierFile().toPath(), Function.identity()));

        //process updates
        Event<Artifact> updateEvent = artifactEvent.select(ArtifactEvent.Literal.UPDATED);
        this.artifacts.replaceAll(c -> loaded.getOrDefault(c.getIdentifierFile().toPath(), c));
        summary.setUpdatedArtifacts(loaded.entrySet().stream()
                .filter(e -> existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet()));

        summary.getUpdatedArtifacts()
                .forEach(((Consumer<Artifact>) updateEvent::fire)
                        .andThen(a -> LOGGER.log(Level.INFO, "Updated artifact " + a)));

        //process unloaded
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        Set<FileBasedArtifact> unloadedComponents = existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        summary.setUnloadedArtifacts(unloadedComponents);
        artifacts.removeAll(unloadedComponents);
        unloadedComponents
                .forEach(((Consumer<Artifact>) unloadEvent::fire)
                        .andThen(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a)));

        //process loaded
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        Set<FileBasedArtifact> loadedComponents = loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        summary.setLoadedArtifacts(loadedComponents);
        artifacts.addAll(loadedComponents);
        loadedComponents.forEach(((Consumer<Artifact>) loadEvent::fire)
                .andThen(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a)));

        return summary;
    }
}
