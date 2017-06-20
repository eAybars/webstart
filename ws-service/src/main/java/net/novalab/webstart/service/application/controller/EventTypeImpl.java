package net.novalab.webstart.service.application.controller;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Created by ertunc on 20/06/17.
 */
public class EventTypeImpl extends AnnotationLiteral<ComponentEvent> implements ComponentEvent {
    private ComponentEvent.Type value;

    public EventTypeImpl(Type value) {
        this.value = value;
    }

    @Override
    public Type value() {
        return value;
    }

}