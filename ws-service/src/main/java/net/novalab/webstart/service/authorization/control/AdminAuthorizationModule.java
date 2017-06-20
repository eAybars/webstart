package net.novalab.webstart.service.authorization.control;

import net.novalab.webstart.service.application.entity.Component;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by ertunc on 31/05/17.
 */
@ApplicationScoped
public class AdminAuthorizationModule implements AuthorizationModule {

    @Inject
    HttpServletRequest request;

    @Override
    public Requirement getRequirement() {
        return Requirement.SUFFICIENT;
    }

    @Override
    public boolean test(Component component) {
        return request.isUserInRole("admin");
    }
}
