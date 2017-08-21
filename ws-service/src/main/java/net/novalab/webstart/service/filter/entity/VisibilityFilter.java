package net.novalab.webstart.service.filter.entity;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface VisibilityFilter {
    class Literal extends AnnotationLiteral<VisibilityFilter> implements VisibilityFilter {
        private static final long serialVersionUID = -5023260083026656920L;
        public static final Literal VALUE = new Literal();
    }
}
