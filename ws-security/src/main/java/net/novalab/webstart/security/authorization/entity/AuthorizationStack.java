package net.novalab.webstart.security.authorization.entity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Iterator;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
public class AuthorizationStack implements Iterable<AuthorizationModule> {

    @Inject
    @Any
    Instance<AuthorizationModule> modules;


    @Override
    public Iterator<AuthorizationModule> iterator() {
        return modules.iterator();
    }
}
