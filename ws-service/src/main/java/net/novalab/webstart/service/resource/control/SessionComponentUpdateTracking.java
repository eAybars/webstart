package net.novalab.webstart.service.resource.control;

import net.novalab.webstart.service.application.controller.ComponentEvent;
import net.novalab.webstart.service.application.entity.Component;
import net.novalab.webstart.service.application.entity.Executable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SessionScoped
public class SessionComponentUpdateTracking implements Serializable {
    private Set<Component> updatedComponents;

    public SessionComponentUpdateTracking() {
        updatedComponents = new HashSet<>();
    }

    public void onComponentUpdate(@Observes @ComponentEvent(ComponentEvent.Type.UPDATED) Executable c) {
        updatedComponents.add(c);
    }

    public void onComponentLoad(@Observes @ComponentEvent(ComponentEvent.Type.UPDATED) Executable c) {
        updatedComponents.add(c);
    }

    public void onComponentUnload(@Observes @ComponentEvent(ComponentEvent.Type.UNLOADED) Executable c) {
        updatedComponents.remove(c);
    }

    public void clearComponentUpdateStatus(Component c) {
        updatedComponents.remove(c);
    }

    public boolean isComponentUpdated(Component c) {
        return updatedComponents.contains(c);
    }
}
