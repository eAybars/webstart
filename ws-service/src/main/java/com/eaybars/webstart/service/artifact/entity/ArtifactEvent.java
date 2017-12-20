package com.eaybars.webstart.service.artifact.entity;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ertunc on 19/06/17.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ArtifactEvent {
    enum Type {
        LOAD,
        UPDATE,
        UNLOAD
    }

    class Literal extends AnnotationLiteral<ArtifactEvent> implements ArtifactEvent {
        public static final ArtifactEvent LOADED = new Literal(Type.LOAD);
        public static final ArtifactEvent UPDATED = new Literal(Type.UPDATE);
        public static final ArtifactEvent UNLOADED = new Literal(Type.UNLOAD);
        private static final long serialVersionUID = -7706750348533258773L;

        private ArtifactEvent.Type value;

        public Literal(Type value) {
            this.value = value;
        }

        @Override
        public Type value() {
            return value;
        }
    }

    Type value();
}
