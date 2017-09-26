package net.novalab.webstart.security.authorization.entity;

import net.novalab.webstart.service.artifact.entity.Artifact;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.util.Optional;

@Stateless
@ModulePriority(10)
public class AdminAuthorizationModule implements AuthorizationModule {
    private static final String ADMIN_ROLE_AUTHENTICATION_REQUIREMENT = "ADMIN_ROLE_AUTHENTICATION_REQUIREMENT";
    private static final String ADMIN_ROLE_NAME = "ADMIN_ROLE_NAME";
    @Resource
    SessionContext sessionContext;

    @Override
    public Requirement getRequirement() {
        try {
            return Requirement.valueOf(Optional.ofNullable(System.getenv(ADMIN_ROLE_AUTHENTICATION_REQUIREMENT))
                    .orElse(System.getProperty(ADMIN_ROLE_AUTHENTICATION_REQUIREMENT, Requirement.SUFFICIENT.name())));
        } catch (IllegalArgumentException e) {
            return Requirement.OPTIONAL;
        }
    }

    @Override
    public boolean test(Artifact artifact) {
        return sessionContext.isCallerInRole(Optional.ofNullable(System.getenv(ADMIN_ROLE_NAME))
                .orElse(System.getProperty(ADMIN_ROLE_NAME, "admin")));
    }

}
