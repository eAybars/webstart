package com.eaybars.webstart.service.artifact.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;
import org.infinispan.manager.CacheContainer;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

@ApplicationScoped
public class Artifacts {
    private static final Logger LOGGER = Logger.getLogger(Artifacts.class.getName());

    @Resource(lookup = "java:jboss/infinispan/container/artifacts")
    CacheContainer cacheContainer;
    Map<URI, Artifact> cache;

    @Inject
    Event<Artifact> artifactEvent;

    @PostConstruct
    private void init() {
        cacheContainer.getCache().addListener(new CacheListener());
        cache = cacheContainer.getCache();
    }


    private Map<URI, Artifact> getCache() {
        return cache;
    }

    /**
     * Returns the artifact for the identifier
     * @param identifier
     * @return
     */
    public Artifact get(URI identifier) {
        return getCache().get(identifier);
    }

    /**
     * Streams all active artifacts
     * @return stream of active artifacts
     */
    public Stream<Artifact> stream() {
        return getCache().values().stream();
    }

    /**
     * Adds the given artifact to the active artifacts and removes and returns the old artifact associated with the same
     * identifier if there exist one
     * @param artifact new artifact to store
     * @return old artifact associate with the same identifier with the given artifact or null if no such artifact exists
     */
    public Artifact put(Artifact artifact) {
        return getCache().put(artifact.getIdentifier(), artifact);
    }

    /**
     * Adds the given artifact to the active artifacts if there is no artifact already associated with the same identifier
     * @param artifact artifact to add
     * @return existing artifact associated with the same identifier or null if no such artifact exists
     */
    public Artifact putIfAbsent(Artifact artifact) {
        return getCache().putIfAbsent(artifact.getIdentifier(), artifact);
    }

    /**
     * Removes the given artifact from the active artifacts
     * @param artifact to remove
     * @return true if the given artifact is among the active artifacts, false otherwise
     */
    public boolean remove(Artifact artifact) {
        return getCache().remove(artifact.getIdentifier()) != null;
    }

    /**
     * Removes the artifact associated with the given identifier from the active artifacts and returns it. If no such
     * artifact exists, null is returned
     * @param identifier identifier of the artifact to be removed
     * @return removed artifact associated with the given identifier or null if no such artifact exists
     */
    public Artifact remove(URI identifier) {
        return getCache().remove(identifier);
    }

    /**
     * Clears active artifacts
     */
    public void removeAll() {
        getCache().clear();
    }

    public Hierarchy hierarchy() {
        return new Hierarchy(stream());
    }

    public Hierarchy hierarchy(Predicate<? super Artifact> filter) {
        return new Hierarchy(stream().filter(filter));
    }

//    public Collection<Artifact> children(Backends.BackendURI target) {
//        SearchManager searchManager = Search.getSearchManager((Cache<?, ?>) getCache(target.getBackend().getName()));
//        QueryBuilder queryBuilder = searchManager.buildQueryBuilderForClass(AbstractArtifact.class).get();
//        Query query = queryBuilder.phrase()
//                .onField("identifier")
//                .sentence(target.getUri().toString())
//                .createQuery();
//        CacheQuery cacheQuery = searchManager.getQuery(query);
//        return (List) cacheQuery.list();
//    }

    public static class Hierarchy {
        private Stream<Artifact> artifacts;

        private Hierarchy(Stream<Artifact> artifacts) {
            this.artifacts = artifacts;
        }

        public Stream<Artifact> top() {
            AtomicReference<Artifact> currentTop = new AtomicReference<>();
            return artifacts.sequential()
                    .sorted(Comparator.naturalOrder())
                    .map(c -> currentTop.accumulateAndGet(c, (oldC, newC) -> oldC == null ? newC :
                            (newC.getIdentifier().toString().startsWith(oldC.getIdentifier().toString()) ? oldC : newC)))
                    .distinct();
        }

        public Optional<Artifact> parent(URI identifier) {
            return parents(identifier)
                    .filter(c -> !identifier.equals(c.getIdentifier()))
                    .findFirst();
        }

        public Stream<Artifact> parents(URI identifier) {
            return artifacts
                    .filter(c -> identifier.toString().startsWith(c.getIdentifier().toString()))
                    .sorted(Comparator.reverseOrder());
        }

        public Stream<Artifact> children(URI identifier) {
            return new Hierarchy(artifacts
                    .filter(a -> a.getIdentifier().toString().startsWith(identifier.toString()))
                    .filter(a -> !identifier.equals(a.getIdentifier()))
            ).top();
        }

        public Stream<Artifact> descendants(URI identifier) {
            return artifacts.filter(a -> a.getIdentifier().toString().startsWith(identifier.toString()));
        }
    }

    @Listener(observation = Listener.Observation.POST)
    public class CacheListener {
        @CacheEntryCreated
        public void added(CacheEntryCreatedEvent<URI, Artifact> event) {
            artifactEvent.select(ArtifactEvent.Literal.LOADED)
                    .fire(event.getValue());
            LOGGER.log(Level.INFO, "Loaded artifact " + event.getValue());
        }

        @CacheEntryModified
        public void modified(CacheEntryModifiedEvent<URI, Artifact> event) {
            artifactEvent.select(ArtifactEvent.Literal.UPDATED)
                    .fire(event.getValue());
            LOGGER.log(Level.INFO, "Updated artifact " + event.getValue());
        }

        @CacheEntryRemoved
        public void removed(CacheEntryRemovedEvent<URI, Artifact> event) {
            artifactEvent.select(ArtifactEvent.Literal.UNLOADED)
                    .fire(event.getOldValue());
            LOGGER.log(Level.INFO, "Unloaded artifact " + event.getOldValue());
        }
    }

}
