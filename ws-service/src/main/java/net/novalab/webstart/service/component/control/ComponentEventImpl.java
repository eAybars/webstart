package net.novalab.webstart.service.component.control;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Created by ertunc on 20/06/17.
 */
public class ComponentEventImpl extends AnnotationLiteral<ComponentEvent> implements ComponentEvent {
    private ComponentEvent.Type value;

    public ComponentEventImpl(Type value) {
        this.value = value;
    }

    @Override
    public Type value() {
        return value;
    }

}