package com.eaybars.webstart.file.schedule.control;

import com.eaybars.webstart.file.action.entity.Action;
import com.eaybars.webstart.file.TemporaryFileAndFolder;
import com.eaybars.webstart.file.backend.control.FileBackend;
import com.eaybars.webstart.file.watch.control.PathWatchService;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;
import com.eaybars.webstart.service.backend.control.BackendArtifacts;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ejb.TimerService;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ActionSchedulerTest {

    private ActionScheduler actionScheduler;
    @Rule
    public TemporaryFileAndFolder temporaryFolder = new TemporaryFileAndFolder();

    private Action a1, a1j, a2, a2j, a2r, g;

    @Before
    public void setUp() throws Exception {
        actionScheduler = new ActionScheduler();
        actionScheduler.timerService = mock(TimerService.class);
        actionScheduler.pathWatchService = mock(PathWatchService.class);
        actionScheduler.backendArtifacts = mock(BackendArtifacts.class);
        actionScheduler.fileBackend = mock(FileBackend.class);

        File app1Folder = temporaryFolder.newFolder("app-1");
        File app1jnlp = temporaryFolder.createFile(app1Folder, "launch.jnlp");

        File app2Folder = temporaryFolder.newFolder("group", "app2");
        File groupFolder = new File(temporaryFolder.getRoot(), "group");
        File app2jnlp = temporaryFolder.createFile(app2Folder, "launch.jnlp");
        File app2Resource = temporaryFolder.createFile(app2Folder, "icon.png");


        a1 = mock(Action.class);
        when(a1.getDomain()).thenReturn(app1Folder.toPath());
        when(a1.getType()).thenReturn(ArtifactEvent.Type.LOAD);
        when(a1.toString()).thenReturn("a1");
        a1j = mock(Action.class);
        when(a1j.getDomain()).thenReturn(app1jnlp.toPath());
        when(a1j.getType()).thenReturn(ArtifactEvent.Type.LOAD);
        when(a1j.toString()).thenReturn("a1j");

        a2 = mock(Action.class);
        when(a2.getDomain()).thenReturn(app2Folder.toPath());
        when(a2.getType()).thenReturn(ArtifactEvent.Type.UPDATE);
        when(a2.toString()).thenReturn("a2");
        a2j = mock(Action.class);
        when(a2j.getDomain()).thenReturn(app2jnlp.toPath());
        when(a2j.getType()).thenReturn(ArtifactEvent.Type.UPDATE);
        when(a2j.toString()).thenReturn("a2j");
        a2r = mock(Action.class);
        when(a2r.getDomain()).thenReturn(app2Resource.toPath());
        when(a2r.getType()).thenReturn(ArtifactEvent.Type.UNLOAD);
        when(a2r.toString()).thenReturn("a2r");

        g = mock(Action.class);
        when(g.getDomain()).thenReturn(groupFolder.toPath());
        when(g.getType()).thenReturn(ArtifactEvent.Type.UNLOAD);
        when(g.toString()).thenReturn("g");

        when(actionScheduler.fileBackend.toURI(app1Folder)).thenReturn(URI.create("/app-1/"));
        when(actionScheduler.fileBackend.toURI(app1jnlp)).thenReturn(URI.create("/app-1/launch.jnlp"));
        when(actionScheduler.fileBackend.toURI(app2Folder)).thenReturn(URI.create("/group/app2/"));
        when(actionScheduler.fileBackend.toURI(app2jnlp)).thenReturn(URI.create("/group/app2/launch.jnlp"));
        when(actionScheduler.fileBackend.toURI(app2Resource)).thenReturn(URI.create("/group/app2/icon.png"));
    }

    @Test
    public void addTestNewAppCreated() throws Exception {
        assertTrue(actionScheduler.add(a1));
        assertFalse(actionScheduler.add(a1j));
        verify(actionScheduler.timerService).createTimer(ActionScheduler.TRIGGER_DURATION, null);
        Path a1Path = a1.getDomain();
        verify(actionScheduler.pathWatchService).register(a1Path);
    }

    @Test
    public void addTestAppUpdated() throws Exception {
        assertTrue(actionScheduler.add(a2j));
        assertNotNull(actionScheduler.getAction(a2j.getDomain()));

        assertTrue(actionScheduler.add(a2));
        assertNotNull(actionScheduler.getAction(a2.getDomain()));
        assertNull(actionScheduler.getAction(a2j.getDomain()));

        assertFalse(actionScheduler.add(a2r));
        assertFalse(actionScheduler.add(a2j));

        verify(actionScheduler.timerService, times(2)).createTimer(ActionScheduler.TRIGGER_DURATION, null);
        Path a2Path = a2.getDomain();
        verify(actionScheduler.pathWatchService).register(a2Path);
        Path a2jPath = a2j.getDomain();
        verify(actionScheduler.pathWatchService, times(0)).register(a2jPath);
        verify(actionScheduler.pathWatchService, times(0)).unregister(a2jPath);
        verify(actionScheduler.pathWatchService, times(0)).unregisterAll(a2jPath);
    }

    @Test
    public void addParentRemoved() throws Exception {
        //populate
        assertTrue(actionScheduler.add(a2j));
        assertTrue(actionScheduler.add(a2r));
        assertTrue(actionScheduler.add(a1));
        //sanity check
        assertNotNull(actionScheduler.getAction(a2j.getDomain()));
        assertNotNull(actionScheduler.getAction(a2r.getDomain()));
        assertNotNull(actionScheduler.getAction(a1.getDomain()));

        //add parent removing
        assertTrue(actionScheduler.add(g));
        assertNull(actionScheduler.getAction(a2j.getDomain()));
        assertNull(actionScheduler.getAction(a2r.getDomain()));
        assertNotNull(actionScheduler.getAction(a1.getDomain()));
        assertNotNull(actionScheduler.getAction(g.getDomain()));
    }

    @Test
    public void loadAndUpdateApp() throws Exception {
        assertTrue(actionScheduler.add(a1));
        when(a1.getType()).thenReturn(ArtifactEvent.Type.UPDATE);
        assertFalse(actionScheduler.add(a1));
    }

    @Test
    public void loadAndUnloadApp() throws Exception {
        assertTrue(actionScheduler.add(a1));

        Path app1Folder = a1.getDomain();

        Action a1 = mock(Action.class);
        when(a1.getDomain()).thenReturn(app1Folder);
        when(a1.getType()).thenReturn(ArtifactEvent.Type.UNLOAD);
        when(a1.toString()).thenReturn("a1");

        assertTrue(actionScheduler.add(a1));
        assertSame(a1, actionScheduler.getAction(app1Folder));
        assertNotSame(this.a1, actionScheduler.getAction(app1Folder));

        assertTrue(actionScheduler.add(this.a1));
        assertSame(this.a1, actionScheduler.getAction(app1Folder));
        assertNotSame(a1, actionScheduler.getAction(app1Folder));
    }


    @Test
    public void findParentAction() {
        actionScheduler.add(a2);
        Optional<Action> parentAction = actionScheduler.findParentAction(a2.getDomain());
        assertTrue(parentAction.isPresent());
        assertSame(a2, parentAction.get());

        parentAction = actionScheduler.findParentAction(a2j.getDomain());
        assertTrue(parentAction.isPresent());
        assertSame(a2, parentAction.get());

        parentAction = actionScheduler.findParentAction(g.getDomain());
        assertFalse(parentAction.isPresent());
    }

    @Test
    public void findActionsUnder() {
        actionScheduler.add(a2j);
        actionScheduler.add(a1);

        Set<Action> actions = actionScheduler.findActionsUnder(a1.getDomain());
        assertEquals(Collections.singleton(a1), actions);

        actions = actionScheduler.findActionsUnder(a2.getDomain());
        assertEquals(Collections.singleton(a2j), actions);

        actions = actionScheduler.findActionsUnder(temporaryFolder.getRoot().toPath());
        assertEquals(2, actions.size());
        assertTrue(actions.contains(a1));
        assertTrue(actions.contains(a2j));
    }

    @Test
    public void remove() {
        actionScheduler.add(a2j);
        actionScheduler.remove(a2j);
        assertNull(actionScheduler.getAction(a2j.getDomain()));
        Path a2jPath = a2j.getDomain();
        verify(actionScheduler.pathWatchService, times(0)).unregisterAll(a2jPath);

        actionScheduler.add(a2);
        actionScheduler.remove(a2);
        assertNull(actionScheduler.getAction(a2.getDomain()));
        Path a2Path = a2.getDomain();
        verify(actionScheduler.pathWatchService, times(1)).unregisterAll(a2Path);
    }

    @Test
    public void cancelAll() {
        actionScheduler.add(a1);
        actionScheduler.add(a2j);

        actionScheduler.cancelAll();
        Path a1Path = a1.getDomain();
        Path a2jPath = a2j.getDomain();
        verify(actionScheduler.pathWatchService, times(1)).unregisterAll(a1Path);
        verify(actionScheduler.pathWatchService, times(0)).unregisterAll(a2jPath);

        assertNull(actionScheduler.getAction(a1Path));
        assertNull(actionScheduler.getAction(a2jPath));
    }

    @Test
    public void executeWithNoAction() {
        actionScheduler.add(a1);
        actionScheduler.add(a2r);
        actionScheduler.add(a2j);

        long time = ActionScheduler.TRIGGER_DURATION / 2;
        when(a1.timeSinceLastAction()).thenReturn(time);
        when(a2r.timeSinceLastAction()).thenReturn(time);
        when(a2j.timeSinceLastAction()).thenReturn(time);
        verify(actionScheduler.timerService, times(3)).createTimer(ActionScheduler.TRIGGER_DURATION, null);

        actionScheduler.execute();
        //verify that no action is taken
        verifyZeroInteractions(actionScheduler.fileBackend);
        assertNotNull(actionScheduler.getAction(a2j.getDomain()));
        assertNotNull(actionScheduler.getAction(a2r.getDomain()));
        assertNotNull(actionScheduler.getAction(a1.getDomain()));
        verify(actionScheduler.timerService, times(4)).createTimer(ActionScheduler.TRIGGER_DURATION, null);
    }

    @Test
    public void executeWithPartialAction() {
        actionScheduler.add(a1);
        actionScheduler.add(a2r);
        actionScheduler.add(a2j);

        when(a1.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION / 2);
        when(a2r.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION * 2);
        when(a2j.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION);
        verify(actionScheduler.timerService, times(3)).createTimer(ActionScheduler.TRIGGER_DURATION, null);

        actionScheduler.execute();
        assertNotNull(actionScheduler.getAction(a1.getDomain()));
        assertNull(actionScheduler.getAction(a2r.getDomain()));
        assertNotNull(actionScheduler.getAction(a2j.getDomain()));
        verify(actionScheduler.timerService, times(4)).createTimer(ActionScheduler.TRIGGER_DURATION, null);
        verify(actionScheduler.backendArtifacts).unload(URI.create("/group/app2/icon.png"));
        verifyNoMoreInteractions(actionScheduler.backendArtifacts);
    }

    @Test
    public void executeAllAction() {
        actionScheduler.add(a1);
        actionScheduler.add(a2r);
        actionScheduler.add(a2j);

        when(a1.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION + 1);
        when(a2r.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION * 2);
        when(a2j.timeSinceLastAction()).thenReturn(ActionScheduler.TRIGGER_DURATION * 2);
        verify(actionScheduler.timerService, times(3)).createTimer(ActionScheduler.TRIGGER_DURATION, null);

        actionScheduler.execute();
        assertNull(actionScheduler.getAction(a1.getDomain()));
        assertNull(actionScheduler.getAction(a2r.getDomain()));
        assertNull(actionScheduler.getAction(a2j.getDomain()));
        verify(actionScheduler.timerService, times(3)).createTimer(ActionScheduler.TRIGGER_DURATION, null);

        verify(actionScheduler.backendArtifacts).unload(URI.create("/group/app2/icon.png"));
        verify(actionScheduler.backendArtifacts).update(URI.create("/group/app2/launch.jnlp"));
        verify(actionScheduler.backendArtifacts).load(URI.create("/app-1/"));
        verifyNoMoreInteractions(actionScheduler.backendArtifacts);
    }
}