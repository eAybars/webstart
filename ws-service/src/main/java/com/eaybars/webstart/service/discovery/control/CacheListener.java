package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.control.ArtifactEvent;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@Listener(observation = Listener.Observation.POST)
public class CacheListener {
    private static final Logger LOGGER = Logger.getLogger(CacheListener.class.getName());

    @Inject
    Event<Artifact> artifactEvent;


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
                .fire(event.getValue());
        LOGGER.log(Level.INFO, "Unloaded artifact " + event.getValue());
    }
}
