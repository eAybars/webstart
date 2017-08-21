package net.novalab.webstart.service.artifact.control;

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
        LOADED,
        UPDATED,
        UNLOADED
    }

    class Literal extends AnnotationLiteral<ArtifactEvent> implements ArtifactEvent {
        public static final Literal LOADED = new Literal(Type.LOADED);
        public static final Literal UPDATED = new Literal(Type.UPDATED);
        public static final Literal UNLOADED = new Literal(Type.UNLOADED);
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
