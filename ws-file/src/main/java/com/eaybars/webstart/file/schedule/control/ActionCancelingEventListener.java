package com.eaybars.webstart.file.schedule.control;

import com.eaybars.webstart.file.backend.control.FileBackend;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;
import java.io.File;

@ApplicationScoped
public class ActionCancelingEventListener {
    @Inject
    ActionScheduler actionScheduler;
    @Inject
    FileBackend fileBackend;

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                  @ArtifactEvent(ArtifactEvent.Type.UNLOAD)
                                          Artifact a) {
        File file = fileBackend.toFile(a.getIdentifier());
        if (file.exists()) {
            actionScheduler.removeAll(actionScheduler.findActionsUnder(file.toPath()));
        }
    }


}
