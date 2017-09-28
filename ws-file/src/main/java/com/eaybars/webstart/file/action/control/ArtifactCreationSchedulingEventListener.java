package com.eaybars.webstart.file.action.control;

import com.eaybars.webstart.file.artifact.entity.FileBasedArtifact;
import com.eaybars.webstart.file.backend.control.ArtifactRoot;
import com.eaybars.webstart.file.backend.control.FileBackend;
import com.eaybars.webstart.file.action.entity.Action;
import com.eaybars.webstart.file.schedule.control.ActionScheduler;
import com.eaybars.webstart.file.watch.control.PathWatchService;
import com.eaybars.webstart.file.watch.entity.PathWatchServiceEvent;
import com.eaybars.webstart.service.artifact.control.ArtifactEvent;
import com.eaybars.webstart.service.discovery.control.BackendArtifactSupplier;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArtifactCreationSchedulingEventListener implements PathWatchService.EventListener {

    @Inject
    ActionScheduler actionScheduler;
    @Inject
    BackendArtifactSupplier backendArtifactSupplier;
    @Inject
    @ArtifactRoot
    File root;


    @Override
    public void accept(PathWatchServiceEvent event) {
        TreeSet<Path> parents = backendArtifactSupplier.getBackendArtifacts(FileBackend.NAME)
                .map(FileBasedArtifact.class::cast)
                .map(FileBasedArtifact::getIdentifierFile)
                .map(File::toPath)
                .filter(event.getPath()::startsWith)
                .collect(Collectors.toCollection(TreeSet::new));

        Path actionPath = parents.isEmpty() ? root.toPath() : parents.last();
        actionScheduler.add(new Action(actionPath, ArtifactEvent.Type.LOAD));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isCreate() && Files.isDirectory(e.getPath());

    }
}
