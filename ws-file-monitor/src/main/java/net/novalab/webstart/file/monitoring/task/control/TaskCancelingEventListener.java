package net.novalab.webstart.file.monitoring.task.control;

import net.novalab.webstart.file.component.entity.FileBasedComponent;
import net.novalab.webstart.service.component.control.ComponentEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.inject.Inject;

@ApplicationScoped
public class TaskCancelingEventListener {
    @Inject
    TaskManager taskManager;

    public void onComponentUnload(@Observes(notifyObserver = Reception.ALWAYS)
                                  @ComponentEvent(ComponentEvent.Type.UNLOADED)
                                          FileBasedComponent c) {
        taskManager.removeAll(taskManager.findTasksUnder(c.getBaseDirectory().toPath()));
    }

}
