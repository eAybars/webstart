package net.novalab.webstart.file.action.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.backend.control.ArtifactRoot;
import net.novalab.webstart.file.backend.control.FileBackend;
import net.novalab.webstart.file.action.entity.Action;
import net.novalab.webstart.file.schedule.control.ActionScheduler;
import net.novalab.webstart.file.watch.control.PathWatchService;
import net.novalab.webstart.file.watch.entity.PathWatchServiceEvent;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.discovery.control.BackendArtifactSupplier;

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
