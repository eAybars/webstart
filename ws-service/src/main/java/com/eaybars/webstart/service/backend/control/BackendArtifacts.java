package com.eaybars.webstart.service.backend.control;

import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.discovery.control.ArtifactDiscovery;
import com.eaybars.webstart.service.uri.control.URIBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

@Singleton
@Startup
public class BackendArtifacts {
    @Inject
    Artifacts artifacts;

    @Inject
    ArtifactDiscovery artifactDiscovery;

    @Resource
    TimerService timerService;

    @PostConstruct
    private void init() {
        TimerConfig config = new TimerConfig();
        config.setPersistent(false);
        timerService.createSingleActionTimer(30000, config);
    }

    @PreDestroy
    private void cancelTimer() {
        timerService.getTimers().forEach(Timer::cancel);
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    private void checkAndLoadAll() {
        if (!artifacts.stream().findAny().isPresent()) {
            loadAll();
        }
    }

    public ArtifactEventSummary reloadAll() {
        return unloadAll().merge(loadAll());
    }

    public ArtifactEventSummary reload(URI uri) {
        return unload(uri).merge(load(uri));
    }

    public ArtifactEventSummary updateAll() {
        return update(Backend.ROOT);
    }

    public ArtifactEventSummary update(URI uri) {
        ArtifactEventSummary summary = new ArtifactEventSummary();
        Set<Artifact> newArtifacts = artifactDiscovery.apply(uri).collect(toSet());

        // gather old artifacts to the "unloaded" section of the result summary
        artifacts.hierarchy().descendants(uri).collect(toCollection(summary::getUnloadedArtifacts));

        for (Artifact newArtifact : newArtifacts) {
            Artifact oldArtifact = artifacts.put(newArtifact);
            if (oldArtifact != null) {
                summary.getUnloadedArtifacts().remove(oldArtifact);
                summary.getUpdatedArtifacts().add(newArtifact);
            } else {
                summary.getLoadedArtifacts().add(newArtifact);
            }
        }

        summary.getUnloadedArtifacts().removeIf(((Predicate<Artifact>)artifacts::remove).negate());

        if (!summary.getLoadedArtifacts().isEmpty()) {
            summary.getLoadedArtifacts().addAll(createMissingParents(uri));
        }

        return summary;
    }

    public ArtifactEventSummary unloadAll() {
        ArtifactEventSummary summary = ArtifactEventSummary.unloadOnly();
        artifacts.stream().collect(Collectors.toCollection(summary::getUnloadedArtifacts));
        artifacts.removeAll();
        return summary;
    }

    public ArtifactEventSummary unload(URI uri) {
        ArtifactEventSummary summary = ArtifactEventSummary.unloadOnly();
        artifacts.hierarchy().descendants(uri)
                .map(Artifact::getIdentifier)
                .map(artifacts::remove)
                .filter(Objects::nonNull)
                .collect(toCollection(summary::getUnloadedArtifacts));
        return summary;
    }

    public ArtifactEventSummary loadAll() {
        return load(Backend.ROOT);
    }

    public ArtifactEventSummary load(URI uri) {
        ArtifactEventSummary summary = ArtifactEventSummary.loadOnly();
        artifactDiscovery.apply(uri)
                .filter(a -> artifacts.putIfAbsent(a) == null)
                .collect(toCollection(summary::getLoadedArtifacts));
        if (!summary.getLoadedArtifacts().isEmpty()) {
            summary.getLoadedArtifacts().addAll(createMissingParents(uri));
        }
        return summary;
    }

    private Set<Artifact> createMissingParents(URI starting) {
        Set<Artifact> created = new HashSet<>();
        try {
            URI parent = URIBuilder.from(starting).addParentPathFromSource().addPath("/").build();
            while (!Backend.ROOT.equals(parent) && artifacts.get(parent) == null) {
                try {
                    Artifact a = new Artifact(parent);
                    if (artifacts.putIfAbsent(a) == null) {
                        created.add(a);
                    }
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                parent = URIBuilder.from(parent).addParentPathFromSource().build();
            }
        } catch (Exception e) {
        }
        return created;
    }
}
