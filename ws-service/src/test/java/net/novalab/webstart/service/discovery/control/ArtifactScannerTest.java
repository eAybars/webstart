package net.novalab.webstart.service.discovery.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.artifact.entity.Component;
import net.novalab.webstart.service.backend.control.Backend;
import net.novalab.webstart.service.backend.control.Backends;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.enterprise.inject.Instance;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class ArtifactScannerTest {
    private ArtifactScanner artifactScanner;
    private ArtifactDiscovery ad1, ad2;
    private Backend backend;
    private Artifact a1, a2;
    private Component c0, c1, c2, c3;
    private URI root = URI.create(""),
            path1 = URI.create("path1/"),
            path1Sub1 = URI.create("path1/path1Sub1/"),
            path1SubSub1 = URI.create("path1/path1Sub1/path1SubSub1"),
            path2 = URI.create("path2/"),
            path2Sub1 = URI.create("path2/path2Sub1");
    private Backends.BackendURI rootBackendURI;

    @Before
    public void setUp() throws Exception {
        artifactScanner = new ArtifactScanner();
        a1 = mock(Artifact.class);
        when(a1.toString()).thenReturn(path1SubSub1.toString());
        a2 = mock(Artifact.class);
        when(a2.toString()).thenReturn(path2Sub1.toString());

        ad1 = mock(ArtifactDiscovery.class);
        when(ad1.apply(any())).thenAnswer(a -> ((Backends.BackendURI) a.getArguments()[0]).getUri().equals(path1SubSub1) ? Stream.of(a1) : Stream.empty());
        ad2 = mock(ArtifactDiscovery.class);
        when(ad2.apply(any())).thenAnswer(a -> ((Backends.BackendURI) a.getArguments()[0]).getUri().equals(path2Sub1) ? Stream.of(a2) : Stream.empty());

        Instance instance = mock(Instance.class);
        when(instance.spliterator()).thenAnswer(a -> Arrays.asList(ad1, ad2).spliterator());
        artifactScanner.discoveries = instance;

        backend = mock(Backend.class);
        when(backend.getName()).thenReturn(URI.create("/test/"));


        c0 = mock(Component.class);
        when(c0.toString()).thenReturn(root.toString());
        c1 = mock(Component.class);
        when(c1.toString()).thenReturn(path1.toString());
        c2 = mock(Component.class);
        when(c2.toString()).thenReturn(path1Sub1.toString());
        c3 = mock(Component.class);
        when(c3.toString()).thenReturn(path2.toString());

        when(backend.createArtifact(Component.class, root)).thenReturn(c0);
        when(backend.createArtifact(Component.class, path1)).thenReturn(c1);
        when(backend.createArtifact(Component.class, path1Sub1)).thenReturn(c2);
        when(backend.createArtifact(Component.class, path2)).thenReturn(c3);


        when(backend.contents(root)).thenAnswer(a -> Stream.of(path1, path2));
        when(backend.contents(path1)).thenAnswer(a -> Stream.of(path1Sub1));
        when(backend.contents(path1Sub1)).thenAnswer(a -> Stream.of(path1SubSub1));
        when(backend.contents(path1SubSub1)).thenAnswer(a -> Stream.empty());
        when(backend.contents(path2)).thenAnswer(a -> Stream.of(path2Sub1));
        when(backend.contents(path2Sub1)).thenAnswer(a -> Stream.empty());

        when(backend.isDirectory(root)).thenReturn(true);
        when(backend.isDirectory(path1)).thenReturn(true);
        when(backend.isDirectory(path1Sub1)).thenReturn(true);
        when(backend.isDirectory(path1SubSub1)).thenReturn(true);
        when(backend.isDirectory(path2)).thenReturn(true);
        when(backend.isDirectory(path2Sub1)).thenReturn(true);

        rootBackendURI = mock(Backends.BackendURI.class);
        when(rootBackendURI.getBackend()).thenReturn(backend);
        when(rootBackendURI.getUri()).thenReturn(root);
        when(rootBackendURI.newBackendURI(any())).thenCallRealMethod();
    }

    @Test
    public void apply() throws Exception {
        Set<? extends Artifact> artifacts = artifactScanner.apply(rootBackendURI).collect(Collectors.toSet());
        assertEquals(6, artifacts.size());
        assertTrue(artifacts.contains(a1));
        assertTrue(artifacts.contains(a2));
        assertTrue(artifacts.contains(c0));
        assertTrue(artifacts.contains(c1));
        assertTrue(artifacts.contains(c2));
        assertTrue(artifacts.contains(c3));

        ArgumentCaptor<Backends.BackendURI> arg = ArgumentCaptor.forClass(Backends.BackendURI.class);

        verify(ad1, times(6)).apply(arg.capture());
        verifyInvocationArguments(arg);

        arg = ArgumentCaptor.forClass(Backends.BackendURI.class);
        verify(ad2, times(6)).apply(arg.capture());
        verifyInvocationArguments(arg);
    }

    private void verifyInvocationArguments(ArgumentCaptor<Backends.BackendURI> arg) {
        List<Backends.BackendURI> uriList = arg.getAllValues();
        assertEquals(backend, uriList.get(0).getBackend());
        assertEquals(backend, uriList.get(1).getBackend());
        assertEquals(backend, uriList.get(2).getBackend());
        assertEquals(backend, uriList.get(3).getBackend());
        assertEquals(backend, uriList.get(4).getBackend());
        assertEquals(backend, uriList.get(5).getBackend());
        assertEquals(root, uriList.get(0).getUri());
        assertEquals(path1, uriList.get(1).getUri());
        assertEquals(path1Sub1, uriList.get(2).getUri());
        assertEquals(path1SubSub1, uriList.get(3).getUri());
        assertEquals(path2, uriList.get(4).getUri());
        assertEquals(path2Sub1, uriList.get(5).getUri());
    }

}