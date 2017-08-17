package net.novalab.webstart.security.authorization.entity;

import net.novalab.webstart.service.component.entity.Component;

import java.util.function.Predicate;

/**
 * Created by ertunc on 30/05/17.
 */
public interface AuthorizationModule extends Predicate<Component> {
    enum Requirement {
        REQUIRED,
        REQUISITE,
        SUFFICIENT,
        OPTIONAL
    }

    Requirement getRequirement();
}
