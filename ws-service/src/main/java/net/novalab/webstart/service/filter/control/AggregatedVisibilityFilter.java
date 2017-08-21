package net.novalab.webstart.service.filter.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.filter.entity.AggregatedFilter;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@AggregatedFilter
@VisibilityFilter
public class AggregatedVisibilityFilter implements Predicate<Artifact> {
    @Inject
    @VisibilityFilter
    Instance<Predicate<Artifact>> componentFilters;

    @Override
    public boolean test(Artifact component) {
        return StreamSupport.stream(componentFilters.spliterator(), false)
                .filter(((Predicate<Object>)AggregatedVisibilityFilter.class::isInstance).negate())
                .allMatch(filter -> filter.test(component));
    }
}
