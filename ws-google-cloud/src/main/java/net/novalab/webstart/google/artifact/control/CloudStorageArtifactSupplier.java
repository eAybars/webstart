package net.novalab.webstart.google.artifact.control;

import net.novalab.webstart.google.artifact.entity.CloudStorageArtifact;
import net.novalab.webstart.google.discovery.control.ArtifactScanner;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.control.ArtifactSupplier;
import net.novalab.webstart.service.artifact.entity.AbstractArtifact;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.ArtifactEventSummary;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
public class CloudStorageArtifactSupplier implements ArtifactSupplier {
    private static final Logger LOGGER = Logger.getLogger(CloudStorageArtifactSupplier.class.getName());

    @Inject
    Event<Artifact> artifactEvent;

    @Inject
    ArtifactScanner artifactScanner;


    private List<CloudStorageArtifact> artifacts;

    public CloudStorageArtifactSupplier() {
        artifacts = new CopyOnWriteArrayList<>();
    }

    @Override
    public Stream<? extends CloudStorageArtifact> get() {
        return artifacts.stream();
    }

    //post construct requires void return
    @PostConstruct
    public void init() {
        reloadAll();
    }

    public ArtifactEventSummary reloadAll() {
        return reload(URI.create("/"));
    }


    public ArtifactEventSummary reload(URI uri) {
        return unload(uri).merge(load(uri));
    }

    public ArtifactEventSummary load(URI uri) {
        List<? extends CloudStorageArtifact> artifacts = artifactScanner.apply(uri)
                .filter(((Predicate<CloudStorageArtifact>) this.artifacts::contains).negate())
                .collect(Collectors.toList());
        this.artifacts.addAll(artifacts);
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
        artifacts.forEach(loadEvent::fire);
        ArtifactEventSummary summary = new ArtifactEventSummary();
        summary.setLoadedArtifacts(artifacts);
        return summary;
    }

    public ArtifactEventSummary unload(URI uri) {
        Set<CloudStorageArtifact> artifacts = this.artifacts.stream()
                .filter(c -> c.getIdentifier().toString().startsWith(uri.toString()))
                .collect(Collectors.toSet());
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        this.artifacts.removeAll(artifacts);
        artifacts.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));
        artifacts.forEach(unloadEvent::fire);
        ArtifactEventSummary summary = new ArtifactEventSummary();
        summary.setUnloadedArtifacts(artifacts);
        return summary;
    }

    public ArtifactEventSummary update(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();

        Map<URI, CloudStorageArtifact> existing = artifacts.stream()
                .filter(c -> c.getIdentifier().toString().startsWith(uri.toString()))
                .collect(Collectors.toMap(AbstractArtifact::getIdentifier, Function.identity()));
        Map<URI, CloudStorageArtifact> loaded = artifactScanner.apply(uri)
                .collect(Collectors.toMap(AbstractArtifact::getIdentifier, Function.identity()));

        //process updates
        Event<Artifact> updateEvent = artifactEvent.select(ArtifactEvent.Literal.UPDATED);
        this.artifacts.replaceAll(c -> loaded.getOrDefault(c.getIdentifier(), c));
        summary.setUpdatedArtifacts(loaded.entrySet().stream()
                .filter(e -> existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList()));
        summary.getUpdatedArtifacts()
                .forEach(((Consumer<Artifact>) updateEvent::fire)
                        .andThen(a -> LOGGER.log(Level.INFO, "Updated artifact " + a)));

        //process unloaded
        Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
        Set<CloudStorageArtifact> unloadedComponents = existing.entrySet().stream()
                .filter(e -> !loaded.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        summary.setUnloadedArtifacts(unloadedComponents);
        artifacts.removeAll(unloadedComponents);
        unloadedComponents.forEach(unloadEvent::fire);
        unloadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));

        //process loaded
        Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
        Set<CloudStorageArtifact> loadedComponents = loaded.entrySet().stream()
                .filter(e -> !existing.containsKey(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
        artifacts.addAll(loadedComponents);
        summary.setLoadedArtifacts(loadedComponents);
        loadedComponents.forEach(loadEvent::fire);
        loadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));

        return summary;
    }
}
