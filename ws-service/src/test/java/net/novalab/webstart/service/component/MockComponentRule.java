package net.novalab.webstart.service.component;

import net.novalab.webstart.service.component.entity.Component;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockComponentRule implements TestRule {

    private List<Component> allComponents;
    private List<Component> filteredComponents;

    public MockComponentRule(Stream<String> identifiers) {
        allComponents = identifiers
                .map(this::toURI)
                .map(id -> {
            Component c = mock(Component.class);
            when(c.getIdentifier()).thenReturn(id);
            when(c.compareTo(any())).thenAnswer(a -> Comparator.<URI>naturalOrder().compare(c.getIdentifier(), ((Component) a.getArguments()[0]).getIdentifier()));
            when(c.toString()).thenReturn(id.toString());
            return c;
        }).collect(Collectors.toList());
        filteredComponents = new ArrayList<>(allComponents.size());
    }

    private URI toURI(String id) {
        try {
            return new URI(id);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                filteredComponents.clear();
                MockComponentFilter filter = description.getAnnotation(MockComponentFilter.class);
                if (filter == null) {
                    filteredComponents.addAll(allComponents);
                } else {
                    Arrays.sort(filter.value());
                    IntStream.range(0, allComponents.size())
                            .filter(i -> Arrays.binarySearch(filter.value(), i) < 0)
                            .mapToObj(allComponents::get)
                            .collect(Collectors.toCollection(() -> filteredComponents));
                }
                base.evaluate();
            }
        };
    }

    public List<Component> getComponents() {
        return filteredComponents;
    }

    public Component get(int index) {
        return allComponents.get(index);
    }
}
