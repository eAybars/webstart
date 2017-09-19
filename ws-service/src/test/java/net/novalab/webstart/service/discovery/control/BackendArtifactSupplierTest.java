package net.novalab.webstart.service.discovery.control;

import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.ArtifactEventSummary;
import net.novalab.webstart.service.backend.control.Backend;
import net.novalab.webstart.service.backend.control.Backends;
import net.novalab.webstart.service.discovery.control.ArtifactScanner;
import net.novalab.webstart.service.discovery.control.BackendArtifactSupplier;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.event.Event;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BackendArtifactSupplierTest {
    private BackendArtifactSupplier backendArtifactSupplier;
    private Event<Artifact> loadEvent, unloadEvent, updateEvent;
    private Backend backend1, backend2;
    private Backends backends;
    private Artifact a1,a2,a3;
    private List<Artifact> b1TopList, b1SubList, b2topList, b2SubList;

    private URI root = URI.create(""),
            b1 = URI.create("/b1/"),
            b2 = URI.create("/b2/"),
            b1Sub = URI.create("/b1/A2/"),
            b2Sub = URI.create("/b2/A3/"),
            b1SubRelative = URI.create("A2/"),
            b2SubRelative = URI.create("A3/");

    @Before
    public void setUp() throws Exception {
        backendArtifactSupplier = new BackendArtifactSupplier();

        backendArtifactSupplier.artifactEvent = mock(Event.class);
        loadEvent = mock(Event.class);
        unloadEvent = mock(Event.class);
        updateEvent = mock(Event.class);
        when(backendArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.LOADED))
                .thenReturn(loadEvent);
        when(backendArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.UPDATED))
                .thenReturn(updateEvent);
        when(backendArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.UNLOADED))
                .thenReturn(unloadEvent);

        a1 = mockArtifact("/A1/");
        a2 = mockArtifact("/A2/");
        a3 = mockArtifact("/A3/");
        b1TopList = Arrays.asList(a1, a2);
        b1SubList = Collections.singletonList(a2);
        b2topList = Collections.singletonList(a3);
        b2SubList = b2topList;

        backendArtifactSupplier.artifactScanner = mock(ArtifactScanner.class);

        backend1 = mock(Backend.class);
        when(backend1.getName()).thenReturn(b1);
        backend2 = mock(Backend.class);
        when(backend2.getName()).thenReturn(b2);

        Backends backends = mock(Backends.class);
        backendArtifactSupplier.backends = backends;
        when(backends.stream()).thenAnswer(a -> Stream.of(backend1, backend2));

        when(backends.toBackendURI(any())).thenReturn(Optional.empty());

        Backends.BackendURI bu = mockBackendURI(backend1, root);
        when(backends.toBackendURI(b1)).thenReturn(Optional.of(bu));
        when(backendArtifactSupplier.artifactScanner.apply(bu)).thenAnswer(a -> b1TopList.stream());

        bu = mockBackendURI(backend2, root);
        when(backends.toBackendURI(b2)).thenReturn(Optional.of(bu));
        when(backendArtifactSupplier.artifactScanner.apply(bu)).thenAnswer(a -> b2topList.stream());

        bu = mockBackendURI(backend1, b1SubRelative);
        when(backends.toBackendURI(b1Sub)).thenReturn(Optional.of(bu));
        when(backendArtifactSupplier.artifactScanner.apply(bu)).thenAnswer(a -> b1SubList.stream());

        bu = mockBackendURI(backend2, b2SubRelative);
        when(backends.toBackendURI(b2Sub)).thenReturn(Optional.of(bu));
        when(backendArtifactSupplier.artifactScanner.apply(bu)).thenAnswer(a -> b2SubList.stream());
    }

    private Backends.BackendURI mockBackendURI(Backend backend, URI uri) {
        Backends.BackendURI backendURI = mock(Backends.BackendURI.class);
        when(backendURI.getBackend()).thenReturn(backend);
        when(backendURI.getUri()).thenReturn(uri);
        when(backendURI.newBackendURI(any())).thenCallRealMethod();
        when(backendURI.isDirectory()).thenReturn(true);
        return backendURI;
    }

    private Artifact mockArtifact(String name) {
        Artifact a = mock(Artifact.class);
        when(a.toString()).thenReturn(name);
        when(a.getIdentifier()).thenReturn(URI.create(name));
        return a;
    }

    @Test
    public void getBackendArtifacts() throws Exception {
        backendArtifactSupplier.loadAll();

        Set<? extends Artifact> artifacts = backendArtifactSupplier.getBackendArtifacts(backend1.getName()).collect(Collectors.toSet());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));

        artifacts = backendArtifactSupplier.getBackendArtifacts(backend2.getName()).collect(Collectors.toSet());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void rootURIs() throws Exception {
        Set<URI> uris = backendArtifactSupplier.rootURIs().collect(Collectors.toSet());
        assertEquals(2, uris.size());
        assertTrue(uris.contains(b1));
        assertTrue(uris.contains(b2));
    }

    @Test
    public void loadAll() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.loadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(3, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));

        summary = backendArtifactSupplier.loadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void loadAllWithPreviouslyLoadedArtifacts() throws Exception {
        b1TopList = Collections.singletonList(a2);
        ArtifactEventSummary summary = backendArtifactSupplier.loadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(2, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));

        b1TopList = Arrays.asList(a1, a2);
        summary = backendArtifactSupplier.loadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a1));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void reloadAll() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.reloadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(3, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));


        summary = backendArtifactSupplier.reloadAll();
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(3, summary.getUnloadedArtifacts().size());
        assertEquals(3, summary.getLoadedArtifacts().size());

        assertTrue(summary.getUnloadedArtifacts().contains(a1));
        assertTrue(summary.getUnloadedArtifacts().contains(a2));
        assertTrue(summary.getUnloadedArtifacts().contains(a3));

        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(updateEvent);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent).fire(a3);
        verify(loadEvent, times(2)).fire(a1);
        verify(loadEvent, times(2)).fire(a2);
        verify(loadEvent, times(2)).fire(a3);


        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void reloadAllWithPreviouslyLoadedArtifacts() throws Exception {
        b1TopList = Collections.singletonList(a2);

        ArtifactEventSummary summary = backendArtifactSupplier.reloadAll();

        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(2, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));


        b1TopList = Arrays.asList(a1, a2);
        summary = backendArtifactSupplier.reloadAll();

        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(2, summary.getUnloadedArtifacts().size());
        assertEquals(3, summary.getLoadedArtifacts().size());

        assertTrue(summary.getUnloadedArtifacts().contains(a2));
        assertTrue(summary.getUnloadedArtifacts().contains(a3));

        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(updateEvent);
        verify(unloadEvent, times(0)).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent).fire(a3);
        verify(loadEvent, times(1)).fire(a1);
        verify(loadEvent, times(2)).fire(a2);
        verify(loadEvent, times(2)).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void updateAll() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.updateAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(3, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));

        summary = backendArtifactSupplier.updateAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());

        assertEquals(3, summary.getUpdatedArtifacts().size());
        assertTrue(summary.getUpdatedArtifacts().contains(a1));
        assertTrue(summary.getUpdatedArtifacts().contains(a2));
        assertTrue(summary.getUpdatedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(updateEvent).fire(a1);
        verify(updateEvent).fire(a2);
        verify(updateEvent).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }

    @Test
    public void updateAllWithPreviouslyLoadedArtifacts() throws Exception {
        b1TopList = Collections.singletonList(a2);

        ArtifactEventSummary summary = backendArtifactSupplier.updateAll();

        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(2, summary.getLoadedArtifacts().size());

        assertTrue(summary.getLoadedArtifacts().contains(a2));
        assertTrue(summary.getLoadedArtifacts().contains(a3));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));


        b1TopList = Arrays.asList(a1, a2);
        summary = backendArtifactSupplier.updateAll();

        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertEquals(2, summary.getUpdatedArtifacts().size());
        assertEquals(1, summary.getLoadedArtifacts().size());

        assertTrue(summary.getUpdatedArtifacts().contains(a2));
        assertTrue(summary.getUpdatedArtifacts().contains(a3));
        assertTrue(summary.getLoadedArtifacts().contains(a1));

        verifyZeroInteractions(unloadEvent);
        verify(updateEvent, times(0)).fire(a1);
        verify(updateEvent).fire(a2);
        verify(updateEvent).fire(a3);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(a3));
    }


    @Test
    public void unloadAll() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.unloadAll();
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());

        backendArtifactSupplier.loadAll();
        summary = backendArtifactSupplier.unloadAll();
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(3, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a2));
        assertTrue(summary.getUnloadedArtifacts().contains(a3));
        assertTrue(summary.getUnloadedArtifacts().contains(a1));

        verifyZeroInteractions(updateEvent);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent).fire(a3);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertTrue(artifacts.isEmpty());
    }

    @Test
    public void reload() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.reload(b1Sub);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verifyZeroInteractions(unloadEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent, times(0)).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a2));


        summary = backendArtifactSupplier.reload(b1);
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(1, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a2));
        assertEquals(2, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertTrue(summary.getLoadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verify(unloadEvent, times(0)).fire(a1);
        verify(unloadEvent, times(1)).fire(a2);
        verify(unloadEvent, times(0)).fire(a3);
        verify(loadEvent, times(1)).fire(a1);
        verify(loadEvent, times(2)).fire(a2);
        verify(loadEvent, times(0)).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
    }

    @Test
    public void load() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.load(b1Sub);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verifyZeroInteractions(unloadEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent, times(0)).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a2));

        summary = backendArtifactSupplier.load(b1);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a1));

        verifyZeroInteractions(updateEvent);
        verifyZeroInteractions(unloadEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent, times(0)).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
    }

    @Test
    public void unloadParent() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.unload(b1);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());

        backendArtifactSupplier.loadAll();
        summary = backendArtifactSupplier.unload(b1);
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertEquals(2, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a1));
        assertTrue(summary.getUnloadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent, times(0)).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a3));

        summary = backendArtifactSupplier.unload(b1Sub);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent, times(0)).fire(a3);

        assertEquals(artifacts, backendArtifactSupplier.get().collect(Collectors.toList()));
    }

    @Test
    public void unload() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.unload(b1Sub);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());

        backendArtifactSupplier.loadAll();
        summary = backendArtifactSupplier.unload(b1Sub);
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertEquals(1, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(unloadEvent, times(0)).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent, times(0)).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a3));

        summary = backendArtifactSupplier.unload(b1);
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertEquals(1, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a1));

        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent, times(0)).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a3));


        summary = backendArtifactSupplier.unload(b2Sub);
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertTrue(summary.getLoadedArtifacts().isEmpty());
        assertEquals(1, summary.getUnloadedArtifacts().size());
        assertTrue(summary.getUnloadedArtifacts().contains(a3));

        verifyZeroInteractions(updateEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent).fire(a3);
        verify(unloadEvent).fire(a1);
        verify(unloadEvent).fire(a2);
        verify(unloadEvent).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertTrue(artifacts.isEmpty());
    }

    @Test
    public void update() throws Exception {
        ArtifactEventSummary summary = backendArtifactSupplier.update(b1Sub);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertTrue(summary.getUpdatedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a2));

        verifyZeroInteractions(updateEvent);
        verifyZeroInteractions(unloadEvent);
        verify(loadEvent, times(0)).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent, times(0)).fire(a3);

        List<? extends Artifact> artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(1, artifacts.size());
        assertTrue(artifacts.contains(a2));

        summary = backendArtifactSupplier.update(b1);
        assertTrue(summary.getUnloadedArtifacts().isEmpty());
        assertEquals(1, summary.getLoadedArtifacts().size());
        assertTrue(summary.getLoadedArtifacts().contains(a1));
        assertEquals(1, summary.getUpdatedArtifacts().size());
        assertTrue(summary.getUpdatedArtifacts().contains(a2));

        verifyZeroInteractions(unloadEvent);
        verify(loadEvent).fire(a1);
        verify(loadEvent).fire(a2);
        verify(loadEvent, times(0)).fire(a3);
        verify(updateEvent, times(0)).fire(a1);
        verify(updateEvent).fire(a2);
        verify(updateEvent, times(0)).fire(a3);

        artifacts = backendArtifactSupplier.get().collect(Collectors.toList());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
    }

}