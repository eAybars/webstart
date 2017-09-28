package com.eaybars.webstart.service.filter.entity;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessFilter {
    class Literal extends AnnotationLiteral<AccessFilter> implements AccessFilter {
        private static final long serialVersionUID = 302673152762293905L;
        public static final Literal VALUE = new Literal();
    }

}
