package com.eaybars.webstart.file.action.control;

import com.eaybars.webstart.file.action.entity.Action;
import com.eaybars.webstart.file.schedule.control.ActionScheduler;
import com.eaybars.webstart.file.watch.control.PathWatchService;
import com.eaybars.webstart.file.watch.entity.PathWatchServiceEvent;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.nio.file.Files;

@ApplicationScoped
public class ArtifactCreationSchedulingEventListener implements PathWatchService.EventListener {

    @Inject
    ActionScheduler actionScheduler;


    @Override
    public void accept(PathWatchServiceEvent event) {
        actionScheduler.add(new Action(event.getPath(), ArtifactEvent.Type.LOAD));
    }

    @Override
    public boolean test(PathWatchServiceEvent e) {
        return e.isCreate() && Files.isDirectory(e.getPath());

    }
}
