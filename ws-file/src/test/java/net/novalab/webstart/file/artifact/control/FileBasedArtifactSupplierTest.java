package net.novalab.webstart.file.artifact.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedExecutable;
import net.novalab.webstart.file.artifact.entity.FileBasedResource;
import net.novalab.webstart.file.discovery.control.ArtifactScanner;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.entity.Artifact;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.mockito.Mockito.*;

import javax.enterprise.event.Event;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class FileBasedArtifactSupplierTest {

    @Rule
    public TemporaryFolder artifactRootRule = new TemporaryFolder();

    private FileBasedArtifactSupplier fileBasedArtifactSupplier;
    private Event<Artifact> loadEvent, unloadEvent, updateEvent;
    private File sub1, sub2;
    private FileBasedExecutable sub1App;
    private FileBasedResource sub2Resource1, sub2Resource2;

    @Before
    public void setUp() throws Exception {
        fileBasedArtifactSupplier = new FileBasedArtifactSupplier();

        fileBasedArtifactSupplier.artifactEvent = mock(Event.class);
        loadEvent = mock(Event.class);
        unloadEvent = mock(Event.class);
        updateEvent = mock(Event.class);
        when(fileBasedArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.LOADED))
                .thenReturn(loadEvent);
        when(fileBasedArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.UPDATED))
                .thenReturn(updateEvent);
        when(fileBasedArtifactSupplier.artifactEvent.select(ArtifactEvent.Literal.UNLOADED))
                .thenReturn(unloadEvent);
        fileBasedArtifactSupplier.artifactScanner = mock(ArtifactScanner.class);

        when(fileBasedArtifactSupplier.artifactScanner.getArtifactRoot()).thenReturn(artifactRootRule.getRoot());

        sub1 = artifactRootRule.newFolder("artifact1");

        sub1App = mock(FileBasedExecutable.class);
        when(sub1App.getIdentifierFile()).thenReturn(sub1);
        when(sub1App.getIdentifier()).thenReturn(new URI("/" + sub1.getName() + "/"));
        when(sub1App.toString()).thenReturn("Application artifact");


        sub2 = artifactRootRule.newFolder("artifact2");

        File r1 = new File(sub2, "resource1.pdf");
        File r2 = new File(sub2, "resource2.pdf");

        if (!r1.createNewFile() || !r2.createNewFile()) {
            throw new RuntimeException("Resource files could not be created");
        }

        sub2Resource1 = mock(FileBasedResource.class);
        when(sub2Resource1.getIdentifierFile()).thenReturn(r1);
        when(sub2Resource1.getIdentifier()).thenReturn(new URI("/" + sub2.getName() + "/" + r1.getName()));
        when(sub2Resource1.toString()).thenReturn("Resource artifact - 1");

        sub2Resource2 = mock(FileBasedResource.class);
        when(sub2Resource2.getIdentifierFile()).thenReturn(r2);
        when(sub2Resource2.getIdentifier()).thenReturn(new URI("/" + sub2.getName() + "/" + r2.getName()));
        when(sub2Resource2.toString()).thenReturn("Resource artifact - 2");

        when(fileBasedArtifactSupplier.artifactScanner.apply(sub1)).thenAnswer(a -> Stream.of(sub1App));
        when(fileBasedArtifactSupplier.artifactScanner.apply(sub2)).thenAnswer(a -> Stream.of(sub2Resource1, sub2Resource2));
    }

    @Test
    public void get() throws Exception {
        assertEquals(0L, fileBasedArtifactSupplier.get().count());
    }

    @Test
    public void findComponent() throws Exception {
        fileBasedArtifactSupplier.reloadAll();

        Optional<FileBasedArtifact> artifact = fileBasedArtifactSupplier.findComponent(sub1.toPath());
        assertNotNull(artifact);
        assertTrue(artifact.isPresent());
        assertEquals(sub1App, artifact.get());

        artifact = fileBasedArtifactSupplier.findComponent(artifactRootRule.getRoot().toPath());
        assertNotNull(artifact);
        assertFalse(artifact.isPresent());

        artifact = fileBasedArtifactSupplier.findComponent(sub2.toPath());
        assertNotNull(artifact);
        assertFalse(artifact.isPresent());

        artifact = fileBasedArtifactSupplier.findComponent(new File(sub2, "resource1.pdf").toPath());
        assertNotNull(artifact);
        assertTrue(artifact.isPresent());
        assertEquals(sub2Resource1, artifact.get());

        artifact = fileBasedArtifactSupplier.findComponent(new File(sub2, "resource2.pdf").toPath());
        assertNotNull(artifact);
        assertTrue(artifact.isPresent());
        assertEquals(sub2Resource2, artifact.get());
    }

    @Test
    public void reloadAll() throws Exception {
        fileBasedArtifactSupplier.reloadAll();

        verify(loadEvent).fire(sub1App);
        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);

        verifyZeroInteractions(updateEvent);
        verifyZeroInteractions(unloadEvent);

        Set<? extends FileBasedArtifact> artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(sub1App));
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));

        when(fileBasedArtifactSupplier.artifactScanner.apply(sub1)).thenAnswer(a -> Stream.empty());
        fileBasedArtifactSupplier.reloadAll();

        verify(unloadEvent).fire(sub1App);
        verify(unloadEvent).fire(sub2Resource1);
        verify(unloadEvent).fire(sub2Resource2);

        verify(loadEvent).fire(sub1App);
        verify(loadEvent, times(2)).fire(sub2Resource1);
        verify(loadEvent, times(2)).fire(sub2Resource2);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));
    }

    @Test
    public void reload() throws Exception {
        fileBasedArtifactSupplier.reload(sub1.toPath());;
        verify(loadEvent).fire(sub1App);
        verify(unloadEvent, times(0)).fire(sub1App);
        verify(loadEvent, times(0)).fire(sub2Resource1);
        verify(loadEvent, times(0)).fire(sub2Resource2);

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);

        Set<? extends FileBasedArtifact> artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.singleton(sub1App), artifacts);

        fileBasedArtifactSupplier.reload(sub1.toPath());;
        verify(unloadEvent).fire(sub1App);
        verify(loadEvent, times(2)).fire(sub1App);
        verify(loadEvent, times(0)).fire(sub2Resource1);
        verify(loadEvent, times(0)).fire(sub2Resource2);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.singleton(sub1App), artifacts);

        when(fileBasedArtifactSupplier.artifactScanner.apply(sub1)).thenAnswer(a -> Stream.empty());
        fileBasedArtifactSupplier.reload(sub1.toPath());;
        verify(unloadEvent, times(2)).fire(sub1App);
        verify(loadEvent, times(2)).fire(sub1App);
        verify(loadEvent, times(0)).fire(sub2Resource1);
        verify(loadEvent, times(0)).fire(sub2Resource2);

        verifyZeroInteractions(updateEvent);
        assertEquals(Collections.emptySet(), fileBasedArtifactSupplier.get().collect(Collectors.toSet()));
    }

    @Test
    public void load() throws Exception {
        fileBasedArtifactSupplier.load(sub1.toPath());;

        verify(loadEvent).fire(sub1App);
        verify(loadEvent, times(0)).fire(sub2Resource1);
        verify(loadEvent, times(0)).fire(sub2Resource2);

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);

        Set<? extends FileBasedArtifact> artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.singleton(sub1App), artifacts);

        fileBasedArtifactSupplier.load(sub1.toPath());;
        verifyNoMoreInteractions(loadEvent);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.singleton(sub1App), artifacts);
    }

    @Test
    public void unload() throws Exception {
        fileBasedArtifactSupplier.unload(sub2.toPath());;

        verifyZeroInteractions(loadEvent);
        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);

        fileBasedArtifactSupplier.load(sub2.toPath());;
        fileBasedArtifactSupplier.load(sub1.toPath());;

        //sanity check
        Set<? extends FileBasedArtifact> artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(sub1App));
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);

        //unload test starts here
        fileBasedArtifactSupplier.unload(sub2.toPath());;

        verify(unloadEvent).fire(sub2Resource1);
        verify(unloadEvent).fire(sub2Resource2);
        verify(unloadEvent, times(0)).fire(sub1App);
        verifyZeroInteractions(updateEvent);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.singleton(sub1App), artifacts);


        fileBasedArtifactSupplier.unload(sub1.toPath());;

        verify(unloadEvent).fire(sub2Resource1);
        verify(unloadEvent).fire(sub2Resource2);
        verify(unloadEvent).fire(sub1App);
        verifyZeroInteractions(updateEvent);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(Collections.emptySet(), artifacts);
    }

    @Test
    public void update() throws Exception {
        fileBasedArtifactSupplier.update(sub2.toPath());;

        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);
        verify(loadEvent, times(0)).fire(sub1App);

        verifyZeroInteractions(unloadEvent);
        verifyZeroInteractions(updateEvent);

        Set<? extends FileBasedArtifact> artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));

        fileBasedArtifactSupplier.update(sub2.toPath());;

        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);
        verify(loadEvent, times(0)).fire(sub1App);

        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);
        verify(loadEvent, times(0)).fire(sub1App);
        verify(updateEvent).fire(sub2Resource1);
        verify(updateEvent).fire(sub2Resource2);
        verify(updateEvent, times(0)).fire(sub1App);

        verifyZeroInteractions(unloadEvent);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));

        fileBasedArtifactSupplier.update(sub1.toPath());;

        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);
        verify(loadEvent).fire(sub1App);
        verify(updateEvent).fire(sub2Resource1);
        verify(updateEvent).fire(sub2Resource2);
        verify(updateEvent, times(0)).fire(sub1App);

        verifyZeroInteractions(unloadEvent);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(3, artifacts.size());
        assertTrue(artifacts.contains(sub1App));
        assertTrue(artifacts.contains(sub2Resource1));
        assertTrue(artifacts.contains(sub2Resource2));

        when(fileBasedArtifactSupplier.artifactScanner.apply(sub2)).thenAnswer(a -> Stream.of(sub2Resource1));
        fileBasedArtifactSupplier.update(sub2.toPath());;

        verify(loadEvent).fire(sub2Resource1);
        verify(loadEvent).fire(sub2Resource2);
        verify(loadEvent).fire(sub1App);
        verify(updateEvent, times(2)).fire(sub2Resource1);
        verify(updateEvent, times(0)).fire(sub1App);
        verify(updateEvent).fire(sub2Resource2);
        verify(unloadEvent, times(0)).fire(sub1App);
        verify(unloadEvent, times(0)).fire(sub2Resource1);
        verify(unloadEvent).fire(sub2Resource2);

        artifacts = fileBasedArtifactSupplier.get().collect(Collectors.toSet());
        assertEquals(2, artifacts.size());
        assertTrue(artifacts.contains(sub1App));
        assertTrue(artifacts.contains(sub2Resource1));
    }

}