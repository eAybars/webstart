package net.novalab.webstart.file.monitoring.task.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.service.artifact.control.ArtifactEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

@ApplicationScoped
public class TaskCancelingEventListener {
    @Inject
    TaskManager taskManager;

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                  @ArtifactEvent(ArtifactEvent.Type.UNLOADED)
                                          FileBasedArtifact c) {
        taskManager.removeAll(taskManager.findTasksUnder(c.getIdentifierFile().toPath()));
    }

}
