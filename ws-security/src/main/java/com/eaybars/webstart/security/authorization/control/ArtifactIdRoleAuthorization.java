package com.eaybars.webstart.security.authorization.control;

import com.eaybars.webstart.security.authorization.entity.ModulePriority;
import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.artifact.entity.Artifact;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Optional;
import java.util.stream.Stream;

@Stateless
@ModulePriority(100)
public class ArtifactIdRoleAuthorization implements AuthorizationModule {
    private static final String URI_ROLE_AUTHENTICATION_REQUIREMENT = "URI_ROLE_AUTHENTICATION_REQUIREMENT";

    @Resource
    SessionContext sessionContext;
    @Inject
    Artifacts artifacts;

    @Override
    public Requirement getRequirement() {
        try {
            return Requirement.valueOf(Optional.ofNullable(System.getenv(URI_ROLE_AUTHENTICATION_REQUIREMENT))
                    .orElse(System.getProperty(URI_ROLE_AUTHENTICATION_REQUIREMENT, Requirement.OPTIONAL.name())));
        } catch (IllegalArgumentException e) {
            return Requirement.OPTIONAL;
        }
    }

    @Override
    public boolean test(Artifact artifact) {
        return Stream.concat(Stream.of(artifact),
                artifacts.hierarchy().parents(artifact.getIdentifier()))
                .map(Artifact::getIdentifier)
                .map(Object::toString)
                .anyMatch(sessionContext::isCallerInRole);
    }
}
