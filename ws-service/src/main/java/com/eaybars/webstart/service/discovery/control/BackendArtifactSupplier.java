package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.control.ArtifactSupplier;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Backends;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class BackendArtifactSupplier implements ArtifactSupplier {

    @Resource(lookup = "java:jboss/infinispan/container/artifacts")
    CacheContainer cacheContainer;

    @Inject
    CacheListener listener;

    @Inject
    ArtifactScanner artifactScanner;

    @Inject
    Backends backends;

    private boolean isSingleBackend;

    @Override
    public Stream<Artifact> get() {
        return backends.stream()
                .map(Backend::getName)
                .map(this::getArtifactsCache)
                .map(Map::values)
                .flatMap(Collection::stream);
    }

    public Stream<Artifact> getBackendArtifacts(URI backendName) {
        return getArtifactsCache(backendName).values().stream();
    }

    public Stream<URI> rootURIs() {
        return backends.stream().map(Backend::getName);
    }

    @PostConstruct
    void init() {
        isSingleBackend = backends.stream().count() < 2;
        backends.stream()
                .map(Backend::getName)
                .forEach(id -> {
                    Cache<URI, Artifact> cache = getArtifactsCache(id);
                    cache.addListener(listener);
                    if (cache.isEmpty()) {
                        reload(id);
                    }
                });
    }

    private Cache<URI, Artifact> getArtifactsCache(URI backendId) {
        return isSingleBackend ? cacheContainer.getCache() : cacheContainer.getCache(backendId.toString());
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
        ArtifactEventSummary summary = ArtifactEventSummary.loadOnly();
        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            if (!backendURI.isDirectory()) {
                throw new IllegalArgumentException(uri + " is not a directory");
            }

            Map<URI, Artifact> artifacts = getArtifactsCache(backendURI.getBackend().getName());
            artifactScanner.apply(backendURI)
                    .filter(a -> artifacts.putIfAbsent(a.getIdentifier(), a) == null)
                    .collect(Collectors.toCollection(summary::getLoadedArtifacts));
        }
        return summary;
    }

    public ArtifactEventSummary unload(URI uri) {
        ArtifactEventSummary summary = ArtifactEventSummary.unloadOnly();

        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            Map<URI, Artifact> artifacts = getArtifactsCache(backendURI.getBackend().getName());

            for (Iterator<Artifact> iterator = artifacts.values().iterator(); iterator.hasNext(); ) {
                Artifact artifact = iterator.next();
                if (artifact.getIdentifier().toString().substring(1).startsWith(backendURI.getUri().toString())) {
                    summary.getUnloadedArtifacts().add(artifact);
                    iterator.remove();
                }
            }
        }
        return summary;
    }

    public ArtifactEventSummary update(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();

        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(uri);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            if (!backendURI.isDirectory()) {
                throw new IllegalArgumentException(uri + " is not a directory");
            }
            Map<URI, Artifact> artifacts = getArtifactsCache(backendURI.getBackend().getName());
            Map<URI, Artifact> newArtifacts = artifactScanner.apply(backendURI).collect(Collectors.toMap(Artifact::getIdentifier, Function.identity()));

            for (Iterator<Map.Entry<URI, Artifact>> iterator = artifacts.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<URI, Artifact> entry = iterator.next();
                if (entry.toString().substring(1).startsWith(backendURI.getUri().toString())) {
                    Artifact newArtifact = newArtifacts.remove(entry.getKey());
                    if (newArtifact == null) {
                        iterator.remove();
                        summary.getUnloadedArtifacts().add(entry.getValue());
                    } else {
                        entry.setValue(newArtifact);
                        summary.getUpdatedArtifacts().add(entry.getValue());
                    }
                }
            }

            newArtifacts.values().stream()
                    .filter(a -> artifacts.putIfAbsent(a.getIdentifier(), a) == null)
                    .collect(Collectors.toCollection(summary::getLoadedArtifacts));
        }

        return summary;
    }

}
