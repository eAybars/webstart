package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.control.ArtifactEvent;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Backends;
import com.eaybars.webstart.service.artifact.control.ArtifactSupplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@ApplicationScoped
public class BackendArtifactSupplier implements ArtifactSupplier {
    private static final Logger LOGGER = Logger.getLogger(BackendArtifactSupplier.class.getName());

    @Inject
    Event<Artifact> artifactEvent;

    @Inject
    ArtifactScanner artifactScanner;

    @Inject
    Backends backends;

    private Map<URI, List<Artifact>> artifacts;

    public BackendArtifactSupplier() {
        artifacts = new ConcurrentHashMap<>();
    }

    @Override
    public Stream<? extends Artifact> get() {
        return artifacts.values().stream()
                .flatMap(Collection::stream);
    }

    public Stream<? extends Artifact> getBackendArtifacts(URI backendName) {
        return artifacts.getOrDefault(backendName, emptyList()).stream();
    }

    public Stream<URI> rootURIs() {
        return backends.stream().map(Backend::getName);
    }

    //post construct requires void return
    @PostConstruct
    public void init() {
        reloadAll();
    }

    public ArtifactEventSummary reloadAll() {
        return rootURIs()
                .map(this::reload)
                .reduce(ArtifactEventSummary::merge)
                .orElseGet(ArtifactEventSummary::new);
    }

    public ArtifactEventSummary updateAll() {
        return rootURIs()
                .map(this::update)
                .reduce(ArtifactEventSummary::merge)
                .orElseGet(ArtifactEventSummary::new);
    }

    public ArtifactEventSummary unloadAll() {
        return rootURIs()
                .map(this::unload)
                .reduce(ArtifactEventSummary::merge)
                .orElseGet(ArtifactEventSummary::new);
    }

    public ArtifactEventSummary loadAll() {
        return rootURIs()
                .map(this::load)
                .reduce(ArtifactEventSummary::merge)
                .orElseGet(ArtifactEventSummary::new);
    }


    public ArtifactEventSummary reload(URI uri) {
        return unload(uri).merge(load(uri));
    }

    public ArtifactEventSummary load(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();
        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            if (!backendURI.isDirectory()) {
                throw new IllegalArgumentException(uri+" is not a directory");
            }

            List<Artifact> artifacts = this.artifacts.computeIfAbsent(backendURI.getBackend().getName(), k -> new CopyOnWriteArrayList<>());
            List<? extends Artifact> discoveredArtifacts = artifactScanner.apply(backendURI)
                    .filter(((Predicate<Artifact>) artifacts::contains).negate())
                    .collect(Collectors.toList());
            artifacts.addAll(discoveredArtifacts);
            Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
            discoveredArtifacts.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
            discoveredArtifacts.forEach(loadEvent::fire);
            summary.setLoadedArtifacts(discoveredArtifacts);
        }
        return summary;
    }

    public ArtifactEventSummary unload(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();

        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();

            URI artifactPattern = URI.create("/" + backendURI.getUri());
            Set<Artifact> artifacts = getBackendArtifacts(backendURI.getBackend().getName())
                    .filter(c -> c.getIdentifier().toString().startsWith(artifactPattern.toString()))
                    .collect(Collectors.toSet());
            Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
            this.artifacts.getOrDefault(backendURI.getBackend().getName(), emptyList()).removeAll(artifacts);
            artifacts.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));
            artifacts.forEach(unloadEvent::fire);
            summary.setUnloadedArtifacts(artifacts);
        }
        return summary;
    }

    public ArtifactEventSummary update(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();

        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            if (!backendURI.isDirectory()) {
                throw new IllegalArgumentException(uri+" is not a directory");
            }
            URI artifactPattern = URI.create("/" + backendURI.getUri());

            Map<URI, Artifact> existing = getBackendArtifacts(backendURI.getBackend().getName())
                    .filter(c -> c.getIdentifier().toString().startsWith(artifactPattern.toString()))
                    .collect(Collectors.toMap(Artifact::getIdentifier, Function.identity()));
            Map<URI, Artifact> loaded = artifactScanner.apply(backendURI)
                    .collect(Collectors.toMap(Artifact::getIdentifier, Function.identity()));

            List<Artifact> artifacts = this.artifacts.computeIfAbsent(backendURI.getBackend().getName(), k -> new CopyOnWriteArrayList<>());

            //process updates
            Event<Artifact> updateEvent = artifactEvent.select(ArtifactEvent.Literal.UPDATED);
            artifacts.replaceAll(c -> loaded.getOrDefault(c.getIdentifier(), c));
            summary.setUpdatedArtifacts(loaded.entrySet().stream()
                    .filter(e -> existing.containsKey(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));
            summary.getUpdatedArtifacts()
                    .forEach(((Consumer<Artifact>) updateEvent::fire)
                            .andThen(a -> LOGGER.log(Level.INFO, "Updated artifact " + a)));

            //process unloaded
            Event<Artifact> unloadEvent = artifactEvent.select(ArtifactEvent.Literal.UNLOADED);
            Set<Artifact> unloadedComponents = existing.entrySet().stream()
                    .filter(e -> !loaded.containsKey(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
            summary.setUnloadedArtifacts(unloadedComponents);
            artifacts.removeAll(unloadedComponents);
            unloadedComponents.forEach(unloadEvent::fire);
            unloadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Unloaded artifact " + a));

            //process loaded
            Event<Artifact> loadEvent = artifactEvent.select(ArtifactEvent.Literal.LOADED);
            Set<Artifact> loadedComponents = loaded.entrySet().stream()
                    .filter(e -> !existing.containsKey(e.getKey()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toSet());
            artifacts.addAll(loadedComponents);
            summary.setLoadedArtifacts(loadedComponents);
            loadedComponents.forEach(loadEvent::fire);
            loadedComponents.forEach(a -> LOGGER.log(Level.INFO, "Loaded artifact " + a));
        }

        return summary;
    }

}
