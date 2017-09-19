package net.novalab.webstart.file.action.control;

import net.novalab.webstart.file.action.entity.Action;
import net.novalab.webstart.file.schedule.control.ActionScheduler;
import net.novalab.webstart.file.watch.control.PathWatchService;
import net.novalab.webstart.file.watch.entity.PathWatchServiceEvent;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;

@ApplicationScoped
public class ArtifactRemovalSchedulingEventListener implements PathWatchService.EventListener {

    @Inject
    ActionScheduler actionScheduler;

    @Override
    public void accept(PathWatchServiceEvent event) {
        actionScheduler.add(new Action(event.getPath(), ArtifactEvent.Type.UNLOAD));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isDelete() && Files.isDirectory(e.getPath());

    }
}
