package net.novalab.webstart.security.authorization.control;

import net.novalab.webstart.security.authorization.entity.AuthorizationModule;
import net.novalab.webstart.security.authorization.entity.ModulePriority;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
public class AuthorizationStack implements Iterable<AuthorizationModule> {

    private List<AuthorizationModule> modules;

    @Inject
    @Any
    @PostConstruct
    public void init(Instance<AuthorizationModule> modules) {
        this.modules = StreamSupport.stream(modules.spliterator(), false)
                .collect(Collectors.toList());
        this.modules.sort(this::compare);
    }

    private int compare(AuthorizationModule m1, AuthorizationModule m2) {
        ModulePriority p1 = m1.getClass().getAnnotation(ModulePriority.class);
        ModulePriority p2 = m2.getClass().getAnnotation(ModulePriority.class);
        return p1 == null
                ? (p2 == null ? 0 : 1)
                : (p2 == null ? -1 : Integer.compare(p1.value(), p2.value()));
    }

    @Override
    public Iterator<AuthorizationModule> iterator() {
        return modules.iterator();
    }
}
