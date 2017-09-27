package net.novalab.webstart.security.authorization.control;

import net.novalab.webstart.security.authorization.entity.ModulePriority;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.filter.entity.AccessFilter;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by ertunc on 30/05/17.
 */
@ApplicationScoped
@AccessFilter
@VisibilityFilter
public class AuthorizationControl implements Predicate<Artifact> {

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
    public boolean test(Artifact component) {
        Iterator<AuthorizationModule> iterator = modules.iterator();
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
