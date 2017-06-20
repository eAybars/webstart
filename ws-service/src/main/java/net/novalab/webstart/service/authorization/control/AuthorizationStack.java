package net.novalab.webstart.service.authorization.control;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
public class AuthorizationStack implements Iterable<AuthorizationModule> {


    @Override
    public Iterator<AuthorizationModule> iterator() {
        return Collections.emptyIterator();
    }
}
