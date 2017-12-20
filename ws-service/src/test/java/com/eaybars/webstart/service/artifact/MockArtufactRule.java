package com.eaybars.webstart.service.artifact;

import com.eaybars.webstart.service.artifact.entity.Artifact;
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

public class MockArtufactRule implements TestRule {

    private List<Artifact> allArtifacts;
    private List<Artifact> filteredArtifacts;

    public MockArtufactRule(Stream<String> identifiers) {
        allArtifacts = identifiers
                .map(this::toURI)
                .map(id -> {
                    Artifact c = mock(Artifact.class);
                    when(c.getIdentifier()).thenReturn(id);
                    when(c.compareTo(any())).thenAnswer(a -> Comparator.<URI>naturalOrder().compare(c.getIdentifier(), ((Artifact) a.getArguments()[0]).getIdentifier()));
                    when(c.toString()).thenReturn(id.toString());
                    return c;
                }).collect(Collectors.toList());
        filteredArtifacts = new ArrayList<>(allArtifacts.size());
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
                filteredArtifacts.clear();
                MockFilter filter = description.getAnnotation(MockFilter.class);
                if (filter == null) {
                    filteredArtifacts.addAll(allArtifacts);
                } else {
                    Arrays.sort(filter.value());
                    IntStream.range(0, allArtifacts.size())
                            .filter(i -> Arrays.binarySearch(filter.value(), i) < 0)
                            .mapToObj(allArtifacts::get)
                            .collect(Collectors.toCollection(() -> filteredArtifacts));
                }
                base.evaluate();
            }
        };
    }

    public List<Artifact> getArtifacts() {
        return filteredArtifacts;
    }

    public Artifact get(int index) {
        return allArtifacts.get(index);
    }
}
