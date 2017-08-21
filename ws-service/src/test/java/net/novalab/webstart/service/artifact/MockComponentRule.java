package net.novalab.webstart.service.artifact;

import net.novalab.webstart.service.artifact.entity.Artifact;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockComponentRule implements TestRule {

    private List<Artifact> allComponents;
    private List<Artifact> filteredComponents;

    public MockComponentRule(Stream<String> identifiers) {
        allComponents = identifiers
                .map(this::toURI)
                .map(id -> {
                    Artifact c = mock(Artifact.class);
                    when(c.getIdentifier()).thenReturn(id);
                    when(c.toRelativePath(any())).thenAnswer(a -> {
                        URI uri = (URI) a.getArguments()[0];
                        if (uri.toString().startsWith(c.getIdentifier().toString())) {
                            return Optional.of(uri.toString().substring(c.getIdentifier().toString().length()));
                        } else {
                            return Optional.empty();
                        }
                    });
                    when(c.compareTo(any())).thenAnswer(a -> Comparator.<URI>naturalOrder().compare(c.getIdentifier(), ((Artifact) a.getArguments()[0]).getIdentifier()));
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
                MockFilter filter = description.getAnnotation(MockFilter.class);
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

    public List<Artifact> getComponents() {
        return filteredComponents;
    }

    public Artifact get(int index) {
        return allComponents.get(index);
    }
}
