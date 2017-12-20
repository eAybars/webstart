package com.eaybars.webstart.service.backend.control;

import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.discovery.control.ArtifactDiscovery;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class BackendArtifactsTest {
    private BackendArtifacts backendArtifacts;
    private Artifacts artifacts;
    private Artifact a1, a2, r1, e1, e2;
    private List<Artifact> b1TopList, b1SubList, b2TopList, b2SubList;
    private Map<URI, Artifact> cache;

    private URI b1 = URI.create("/b1/"),
            b2 = URI.create("/b2/"),
            b1Sub = URI.create("/b1/A2/"),
            b2Sub = URI.create("/b2/A3/");

    @Before
    public void setUp() throws Exception {
        a1 = mockArtifact("/b1/");
        a2 = mockArtifact("/b2/");
        r1 = mockArtifact("/b1/a1.pdf");
        e1 = mockArtifact("/b1/A2/");
        e2 = mockArtifact("/b2/A3/");
        b1TopList = Arrays.asList(a1, r1, e1);
        b1SubList = Collections.singletonList(e1);
        b2TopList = Arrays.asList(a2, e2);
        b2SubList = Collections.singletonList(e2);

        cache = new ConcurrentHashMap<>();
        artifacts = mock(Artifacts.class);
        when(artifacts.get(any(URI.class))).thenAnswer(a -> cache.get(a.getArguments()[0]));
        when(artifacts.stream()).thenAnswer(a -> cache.values().stream());
        when(artifacts.put(any(Artifact.class))).thenAnswer(a -> cache.put(((Artifact)a.getArguments()[0]).getIdentifier(), (Artifact)a.getArguments()[0]));
        when(artifacts.putIfAbsent(any(Artifact.class))).thenAnswer(a -> cache.putIfAbsent(((Artifact)a.getArguments()[0]).getIdentifier(), (Artifact)a.getArguments()[0]));
        when(artifacts.remove(any(Artifact.class))).thenAnswer(a -> cache.remove(((Artifact)a.getArguments()[0]).getIdentifier()) != null);
        when(artifacts.remove(any(URI.class))).thenAnswer(a -> cache.remove((URI)a.getArguments()[0]));
        doAnswer(a -> {
            cache.clear();
            return null;
        }).when(artifacts).removeAll();

        Artifacts.Hierarchy hierarchy = mock(Artifacts.Hierarchy.class);
        when(artifacts.hierarchy()).thenReturn(hierarchy);
        when(hierarchy.descendants(any(URI.class))).thenAnswer(a -> Stream.empty());

        backendArtifacts = new BackendArtifacts();
        backendArtifacts.artifacts = artifacts;

        backendArtifacts.artifactDiscovery = mock(ArtifactDiscovery.class);
        when(backendArtifacts.artifactDiscovery.apply(Backend.ROOT)).thenAnswer(a -> Stream.concat(b1TopList.stream(), b2TopList.stream()));
        when(backendArtifacts.artifactDiscovery.apply(b1)).thenAnswer(a -> b1TopList.stream());
        when(backendArtifacts.artifactDiscovery.apply(b2)).thenAnswer(a -> b2TopList.stream());
        when(backendArtifacts.artifactDiscovery.apply(b1Sub)).thenAnswer(a -> b1SubList.stream());
        when(backendArtifacts.artifactDiscovery.apply(b2Sub)).thenAnswer(a -> b2SubList.stream());
    }

    private void mockArtifacts() {
        cache.put(a1.getIdentifier(), a1);
        cache.put(a2.getIdentifier(), a2);
        cache.put(r1.getIdentifier(), r1);
        cache.put(e1.getIdentifier(), e1);
        cache.put(e2.getIdentifier(), e2);

        Artifacts.Hierarchy hierarchy = artifacts.hierarchy();
        when(hierarchy.descendants(Backend.ROOT)).thenAnswer(a -> cache.values().stream());
        when(hierarchy.descendants(b1)).thenAnswer(a -> b1TopList.stream());
        when(hierarchy.descendants(b1Sub)).thenAnswer(a -> b1SubList.stream());
        when(hierarchy.descendants(b2)).thenAnswer(a -> b2TopList.stream());
        when(hierarchy.descendants(b2Sub)).thenAnswer(a -> b2SubList.stream());
    }

    private void mockArtifactsPartially() {
        mockArtifacts();
        cache.values().removeAll(b2TopList);

        Artifacts.Hierarchy hierarchy = artifacts.hierarchy();
        when(hierarchy.descendants(b2)).thenAnswer(a -> Stream.empty());
        when(hierarchy.descendants(b2Sub)).thenAnswer(a -> Stream.empty());
    }


    private Artifact mockArtifact(String name) {
        Artifact a = mock(Artifact.class);
        when(a.toString()).thenReturn(name);
        when(a.getIdentifier()).thenReturn(URI.create(name));
        return a;
    }

    private static <T> Set<T> setOf(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    @Test
    public void reloadAllFromFullUnloaded() {
        ArtifactEventSummary summary = backendArtifacts.reloadAll();
        Set<Artifact> loaded = setOf(a1, a2, e1, e2, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void reloadAllFromFullLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.reloadAll();

        Set<Artifact> set = setOf(a1, a2, e1, e2, r1);

        assertEquals(set, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(set, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(set.containsAll(cache.values()));
    }

    @Test
    public void reloadAllWithMissingFromFullLoaded() {
        mockArtifacts();
        when(backendArtifacts.artifactDiscovery.apply(Backend.ROOT)).thenAnswer(a -> Stream.of(a1, a2, e1, e2));

        ArtifactEventSummary summary = backendArtifacts.reloadAll();

        Set<Artifact> set = setOf(a1, a2, e1, e2);

        assertEquals(setOf(a1, a2, e1, e2, r1), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(set, summary.getLoadedArtifacts());

        assertEquals(4, cache.size());
        assertTrue(set.containsAll(cache.values()));
    }

    @Test
    public void reloadAllFromPartialLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.reloadAll();

        Set<Artifact> set = setOf(a1, a2, e1, e2, r1);

        assertEquals(setOf(a1, r1, e1), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(set, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(set.containsAll(cache.values()));
    }

    @Test
    public void reloadRootFromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.reload(Backend.ROOT);
        Set<Artifact> loaded = setOf(a1, a2, e1, e2, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void reloadB1FromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.reload(b1);
        Set<Artifact> loaded = setOf(a1, r1, e1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(3, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void reloadB1SubFromEmpty() throws URISyntaxException {
        ArtifactEventSummary summary = backendArtifacts.reload(b1Sub);
        Set<Artifact> loaded = setOf(new Artifact(new URI("/b1/")), e1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(2, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void reloadRootFromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.reload(Backend.ROOT);
        Set<Artifact> set = setOf(a1, a2, e1, e2, r1);

        assertEquals(set, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(set, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(set.containsAll(cache.values()));
    }

    @Test
    public void reloadB1FromFullyLoaded() {
        mockArtifacts();

        ArtifactEventSummary summary = backendArtifacts.reload(b1);
        Set<Artifact> loaded = setOf(a1, r1, e1);

        assertEquals(loaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(loaded));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void reloadB1SubFromFullyLoaded() throws URISyntaxException {
        mockArtifacts();

        ArtifactEventSummary summary = backendArtifacts.reload(b1Sub);
        Set<Artifact> loaded = setOf(e1);

        assertEquals(loaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(loaded));
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void reloadRootFromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.reload(Backend.ROOT);

        Set<Artifact> set = setOf(a1, a2, e1, e2, r1);

        assertEquals(setOf(a1, r1, e1), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(set, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(set.containsAll(cache.values()));
    }

    @Test
    public void reloadB1FromPartiallyLoaded() {
        mockArtifactsPartially();

        ArtifactEventSummary summary = backendArtifacts.reload(b1);
        Set<Artifact> loaded = setOf(a1, r1, e1);

        assertEquals(loaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(3, cache.size());
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void reloadB1SubFromPartiallyLoaded() throws URISyntaxException {
        mockArtifactsPartially();

        ArtifactEventSummary summary = backendArtifacts.reload(b1Sub);
        Set<Artifact> loaded = setOf(e1);

        assertEquals(loaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(3, cache.size());
        assertTrue(cache.values().containsAll(loaded));
        assertTrue(cache.values().containsAll(b1TopList));
    }

    @Test
    public void reloadB2FromPartiallyLoaded() {
        mockArtifactsPartially();

        ArtifactEventSummary summary = backendArtifacts.reload(b2);
        Set<Artifact> loaded = setOf(a2, e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(loaded));
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void reloadB2SubFromPartiallyLoaded() throws URISyntaxException {
        mockArtifactsPartially();

        ArtifactEventSummary summary = backendArtifacts.reload(b2Sub);
        Set<Artifact> loaded = setOf(new Artifact(new URI("/b2/")), e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(loaded));
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void updateAllFromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.updateAll();
        Set<Artifact> loaded = setOf(a1, a2, e1, e2, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void updateAllFromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.updateAll();
        Set<Artifact> loaded = setOf(a1, a2, e1, e2, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(loaded, summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(loaded.containsAll(cache.values()));
    }

    @Test
    public void updateAllWithUnloadFromFullyLoaded() {
        mockArtifacts();
        when(backendArtifacts.artifactDiscovery.apply(Backend.ROOT)).thenAnswer(a -> Stream.of(a1, a2, e1, e2));

        ArtifactEventSummary summary = backendArtifacts.updateAll();
        Set<Artifact> loaded = setOf(a1, a2, e1, e2);

        assertEquals(setOf(r1), summary.getUnloadedArtifacts());
        assertEquals(loaded, summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(4, cache.size());
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void updateAllFromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.updateAll();
        Set<Artifact> updated = setOf(a1, e1, r1);
        Set<Artifact> loaded = setOf(a2, e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(updated, summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(updated));
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void updateB1FromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.update(b1);
        Set<Artifact> updated = setOf(a1, e1, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(updated, summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void updateB1FromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.update(b1);
        Set<Artifact> updated = setOf(a1, e1, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(updated, summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(3, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
    }

    @Test
    public void updateB2FromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.update(b2);
        Set<Artifact> loaded = setOf(a2, e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void updateB2SubFromPartiallyLoaded() throws URISyntaxException {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.update(b2Sub);
        Set<Artifact> loaded = setOf(new Artifact(new URI("/b2/")), e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(loaded));
    }

    @Test
    public void unloadAllFromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.unloadAll();
        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());
        assertTrue(cache.isEmpty());
    }

    @Test
    public void unloadAllFromLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.unloadAll();
        Set<Artifact> unloaded = setOf(a1, a2, e1, e2, r1);

        assertEquals(unloaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertTrue(cache.isEmpty());
    }

    @Test
    public void unloadB1() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.unload(b1);
        Set<Artifact> unloaded = setOf(a1, e1, r1);

        assertEquals(unloaded, summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(2, cache.size());
        assertTrue(cache.containsValue(a2));
        assertTrue(cache.containsValue(e2));
    }

    @Test
    public void loadAllFromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.loadAll();
        Set<Artifact> loaded = setOf(a1, e1, a2, e2, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void loadAllFromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.loadAll();

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void loadAllFromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.loadAll();
        Set<Artifact> loaded = setOf(a2, e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void loadB1FromEmpty() {
        ArtifactEventSummary summary = backendArtifacts.load(b1);
        Set<Artifact> loaded = setOf(a1, e1, r1);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(3, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
    }

    @Test
    public void loadB1FromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.load(b1);
        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void loadB2FromFullyLoaded() {
        mockArtifacts();
        ArtifactEventSummary summary = backendArtifacts.load(b2);
        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(Collections.emptySet(), summary.getLoadedArtifacts());

        assertEquals(5, cache.size());
        assertTrue(cache.values().containsAll(b1TopList));
        assertTrue(cache.values().containsAll(b2TopList));
    }

    @Test
    public void loadB2FromPartiallyLoaded() {
        mockArtifactsPartially();
        ArtifactEventSummary summary = backendArtifacts.load(b2);
        Set<Artifact> loaded = setOf(a2, e2);

        assertEquals(Collections.emptySet(), summary.getUnloadedArtifacts());
        assertEquals(Collections.emptySet(), summary.getUpdatedArtifacts());
        assertEquals(loaded, summary.getLoadedArtifacts());

        assertEquals(5, this.cache.size());
        assertTrue(this.cache.values().containsAll(b1TopList));
        assertTrue(this.cache.values().containsAll(b2TopList));
    }
}