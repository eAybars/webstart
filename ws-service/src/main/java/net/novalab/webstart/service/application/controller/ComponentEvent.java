package net.novalab.webstart.service.application.controller;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ertunc on 19/06/17.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentEvent {
    enum Type {
        LOADED,
        UPDATED,
        UNLOADED
    }

    Type value();
}
