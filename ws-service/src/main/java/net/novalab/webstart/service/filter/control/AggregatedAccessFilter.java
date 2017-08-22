package net.novalab.webstart.service.filter.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.filter.entity.AccessFilter;
import net.novalab.webstart.service.filter.entity.AggregatedFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@AggregatedFilter
@AccessFilter
@ApplicationScoped
public class AggregatedAccessFilter implements Predicate<Artifact> {
    @Inject
    @AccessFilter
    Instance<Predicate<Artifact>> componentFilters;

    @Override
    public boolean test(Artifact component) {
        return StreamSupport.stream(componentFilters.spliterator(), false)
                .filter(((Predicate<Object>)AggregatedAccessFilter.class::isInstance).negate())
                .allMatch(filter -> filter.test(component));
    }
}
