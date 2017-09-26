package net.novalab.webstart.security.authorization.control;

import net.novalab.webstart.security.authorization.entity.AuthorizationModule;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.filter.entity.AccessFilter;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
@AccessFilter
@VisibilityFilter
public class AuthorizationControl implements Predicate<Artifact> {

    @Inject
    AuthorizationStack authorizationStack;

    @Override
    public boolean test(Artifact component) {
        Iterator<AuthorizationModule> iterator = authorizationStack.iterator();
        boolean optionalStatus = false;
        boolean nonOptionalStatus = true;
        boolean reqModulesConfigured = false;

        while (iterator.hasNext()) {
            AuthorizationModule module = iterator.next();
            boolean moduleStatus = module.test(component);
            switch (module.getRequirement()) {
                case OPTIONAL:
                    optionalStatus |= moduleStatus;
                    break;
                case REQUIRED:
                    reqModulesConfigured = true;
                    nonOptionalStatus &= moduleStatus;
                    break;
                case SUFFICIENT:
                    if (nonOptionalStatus && moduleStatus) return true;
                    break;
                case REQUISITE:
                    reqModulesConfigured = true;
                    if (!moduleStatus) return false;
                    else nonOptionalStatus &= true;
            }
        }
        return reqModulesConfigured ? nonOptionalStatus : optionalStatus;
    }
}
