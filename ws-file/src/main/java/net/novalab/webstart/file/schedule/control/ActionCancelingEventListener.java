package net.novalab.webstart.file.schedule.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

@ApplicationScoped
public class ActionCancelingEventListener {
    @Inject
    ActionScheduler actionScheduler;

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                  @ArtifactEvent(ArtifactEvent.Type.UNLOAD)
                                          FileBasedArtifact c) {
        actionScheduler.removeAll(actionScheduler.findActionsUnder(c.getIdentifierFile().toPath()));
    }


}
