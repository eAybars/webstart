package net.novalab.webstart.service.filter.control;

import net.novalab.webstart.service.component.entity.Component;
import net.novalab.webstart.service.filter.entity.AggregatedFilter;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@AggregatedFilter
@VisibilityFilter
public class AggregatedVisibilityFilter implements Predicate<Component> {
    @Inject
    @VisibilityFilter
    Instance<Predicate<Component>> componentFilters;

    @Override
    public boolean test(Component component) {
        return StreamSupport.stream(componentFilters.spliterator(), false)
                .filter(((Predicate<Object>)AggregatedVisibilityFilter.class::isInstance).negate())
                .allMatch(filter -> filter.test(component));
    }
}
