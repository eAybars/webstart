package com.eaybars.webstart.service.discovery.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.Executable;
import com.eaybars.webstart.service.artifact.entity.Resource;
import com.eaybars.webstart.service.backend.control.Backend;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class ArtifactDiscoveryTest {
    private ArtifactDiscovery artifactDiscovery;
    private ArtifactCreator artifactCreator1, artifactCreator2;
    private Backend backend;
    private Artifact resource, executable;
    private URI root = URI.create("/"),
            path1 = URI.create("/path1/"),
            path1Sub1 = URI.create("/path1/path1Sub1/"),
            path1SubSub1 = URI.create("/path1/path1Sub1/path1SubSub1.pdf"),
            path2 = URI.create("/path2/"),
            path2Sub1 = URI.create("/path2/app.jnlp/");

    @Before
    public void setUp() throws Exception {
        artifactDiscovery = new ArtifactDiscovery();
        resource = mock(Resource.class);
        when(resource.getIdentifier()).thenReturn(path1SubSub1);
        when(resource.toString()).thenReturn(path1SubSub1.toString());

        executable = mock(Executable.class);
        when(executable.getIdentifier()).thenReturn(path2);
        when(executable.toString()).thenReturn(path2.toString());

        artifactCreator1 = mock(ArtifactCreator.class);
        when(artifactCreator1.toString()).thenReturn("artifactCreator1");
        when(artifactCreator1.apply(any(), any())).thenAnswer(a -> ((List<URI>) a.getArguments()[1]).contains(path1SubSub1) ? Stream.of(resource) : Stream.empty());
        artifactCreator2 = mock(ArtifactCreator.class);
        when(artifactCreator2.toString()).thenReturn("artifactCreator2");
        when(artifactCreator2.apply(any(), any())).thenAnswer(a -> ((List<URI>) a.getArguments()[1]).contains(path2Sub1) ? Stream.of(executable) : Stream.empty());

        Instance instance = mock(Instance.class);
        when(instance.spliterator()).thenAnswer(a -> Arrays.asList(artifactCreator1, artifactCreator2).spliterator());
        artifactDiscovery.discoveries = instance;

        backend = mock(Backend.class);
        when(backend.getName()).thenReturn(URI.create("/test/"));

        when(backend.contents(root)).thenAnswer(a -> Stream.of(path1, path2));
        when(backend.contents(path1)).thenAnswer(a -> Stream.of(path1Sub1));
        when(backend.contents(path1Sub1)).thenAnswer(a -> Stream.of(path1SubSub1));
        when(backend.contents(path1SubSub1)).thenAnswer(a -> Stream.empty());
        when(backend.contents(path2)).thenAnswer(a -> Stream.of(path2Sub1));
        when(backend.contents(path2Sub1)).thenAnswer(a -> Stream.empty());

        when(backend.isDirectory(root)).thenReturn(true);
        when(backend.isDirectory(path1)).thenReturn(true);
        when(backend.isDirectory(path1Sub1)).thenReturn(true);
        when(backend.isDirectory(path1SubSub1)).thenReturn(false);
        when(backend.isDirectory(path2)).thenReturn(true);
        when(backend.isDirectory(path2Sub1)).thenReturn(false);

        artifactDiscovery.backend = backend;
    }

    @Test
    public void apply() throws Exception {
        Set<? extends Artifact> artifacts = artifactDiscovery.apply(root).collect(Collectors.toSet());
        assertEquals(4, artifacts.size());
        assertTrue(artifacts.contains(resource));
        assertTrue(artifacts.contains(executable));
        assertTrue(artifacts.contains(new Artifact(path1)));
        assertTrue(artifacts.contains(new Artifact(path1Sub1)));
    }

    @Test
    public void applySingleResource() throws Exception {
        //first test that nothing happens because no resource exists
        Set<Artifact> artifacts = artifactDiscovery.apply(path1SubSub1).collect(Collectors.toSet());
        assertTrue(artifacts.isEmpty());
        verifyZeroInteractions(artifactCreator1);
        verifyZeroInteractions(artifactCreator2);

        //ensure resource exists
        when(backend.getResource(path1SubSub1)).thenReturn(new URL("http://somedomain.com/path1/path1Sub1/path1SubSub1.pdf"));

        artifacts = artifactDiscovery.apply(path1SubSub1).collect(Collectors.toSet());
        assertEquals(Collections.singleton(resource), artifacts);

        verify(artifactCreator1).apply(backend, Collections.singletonList(path1SubSub1));
        verifyNoMoreInteractions(artifactCreator1);
        verify(artifactCreator2).apply(backend, Collections.singletonList(path1SubSub1));
        verifyNoMoreInteractions(artifactCreator2);
    }
}