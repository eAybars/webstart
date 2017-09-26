package net.novalab.webstart.security.authorization.entity;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.TYPE)
public @interface ModulePriority {
    int value();
}
