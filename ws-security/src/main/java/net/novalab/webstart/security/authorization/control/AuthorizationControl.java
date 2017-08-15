package net.novalab.webstart.security.authorization.control;

import net.novalab.webstart.security.authorization.entity.AuthorizationModule;
import net.novalab.webstart.security.authorization.entity.AuthorizationStack;
import net.novalab.webstart.service.application.entity.Component;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
public class AuthorizationControl implements Predicate<Component> {

    @Inject
    AuthorizationStack authorizationStack;

    @Override
    public boolean test(Component component) {
        Iterator<AuthorizationModule> iterator = authorizationStack.iterator();
        boolean optionalStatus = false;
        boolean nonOptionalStatus = true;

        while (iterator.hasNext()) {
            AuthorizationModule module = iterator.next();
            boolean moduleStatus = module.test(component);
            switch (module.getRequirement()) {
                case OPTIONAL:
                    optionalStatus |= moduleStatus;
                    break;
                case REQUIRED:
                    nonOptionalStatus &= moduleStatus;
                    break;
                case SUFFICIENT:
                    if (nonOptionalStatus && moduleStatus) return true;
                    break;
                case REQUISITE:
                    if (!moduleStatus) return false;
                    else nonOptionalStatus &= true;
            }
        }
        return nonOptionalStatus || optionalStatus;
    }
}
