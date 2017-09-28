package com.eaybars.webstart.security.authorization.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;

import java.util.function.Predicate;

/**
 * Created by ertunc on 30/05/17.
 */
public interface AuthorizationModule extends Predicate<Artifact> {
    enum Requirement {
        REQUIRED,
        REQUISITE,
        SUFFICIENT,
        OPTIONAL
    }

    Requirement getRequirement();
}
