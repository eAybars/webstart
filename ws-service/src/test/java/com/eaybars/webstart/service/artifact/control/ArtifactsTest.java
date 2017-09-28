package com.eaybars.webstart.service.artifact.control;

import com.eaybars.webstart.service.artifact.MockComponentRule;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtifactsTest {
    @Rule
    public MockComponentRule mockComponents = new MockComponentRule(Stream.of(
            "/domain1/",                    //0
            "/something/",
            "/domain2/",                    //2
            "/domain1/sub1/",
            "/domain2/sub1/",               //4
            "/domain1/sub1/sub-sub1/",
            "/domain2/sub1/sub-sub1/",      //6
            "/other/something/",
            "/sub1/domain2/sub-sub1/",      //8
            "/domain1/sub1/sub-sub2/"
    ));

    private Artifacts artifacts;

    @Before
    public void setUp() throws URISyntaxException {
        artifacts = new Artifacts();

        ArtifactSupplier s1 = mock(ArtifactSupplier.class);
        when(s1.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(0),
                mockComponents.get(3),
                mockComponents.get(5))
        );

        ArtifactSupplier s2 = mock(ArtifactSupplier.class);
        when(s2.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(2),
                mockComponents.get(4),
                mockComponents.get(6))
        );


        ArtifactSupplier s3 = mock(ArtifactSupplier.class);
        when(s3.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(1),
                mockComponents.get(9),
                mockComponents.get(7),
                mockComponents.get(8))
        );

        artifacts.componentSuppliers = mock(Instance.class);
        when(artifacts.componentSuppliers.spliterator()).thenAnswer(a -> Arrays.asList(s1, s2, s3).spliterator());
    }

    @Test
    public void stream() throws Exception {
        assertEquals(10, artifacts.stream().count());
        assertTrue(artifacts.stream().collect(Collectors.toSet()).containsAll(mockComponents.getComponents()));
    }

    @Test
    public void top() throws Exception {
        Set<? extends Artifact> set = artifacts.hierarchy().top().collect(Collectors.toSet());
        assertEquals(5, set.size());
        assertTrue(set.containsAll(Arrays.asList(
                mockComponents.get(0),
                mockComponents.get(1),
                mockComponents.get(2),
                mockComponents.get(7),
                mockComponents.get(8)
        )));
    }

    @Test
    public void topWithFilter() throws Exception {
        Set<? extends Artifact> set = artifacts.hierarchy(
                c -> mockComponents.get(2) != c && mockComponents.get(9) != c
        ).top().collect(Collectors.toSet());
        assertEquals(5, set.size());
        assertTrue(set.containsAll(Arrays.asList(
                mockComponents.get(0),
                mockComponents.get(1),
                mockComponents.get(4),
                mockComponents.get(7),
                mockComponents.get(8)
        )));
    }

    @Test
    public void parent() throws Exception {
        Optional<Artifact> optional = artifacts.hierarchy().parent(URI.create("/abc/"));
        assertNotNull(optional);
        assertFalse(optional.isPresent());

        //no parent for a top component
        optional = artifacts.hierarchy().parent(URI.create("/domain1/"));
        assertNotNull(optional);
        assertFalse(optional.isPresent());

        optional = artifacts.hierarchy().parent(URI.create("/domain1/sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockComponents.get(0), optional.get());

        optional = artifacts.hierarchy().parent(URI.create("/domain1/xyz/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockComponents.get(0), optional.get());

        optional = artifacts.hierarchy().parent(URI.create("/domain1/sub1/sub-sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockComponents.get(3), optional.get());
    }

    @Test
    public void parentWithFilter() throws Exception {
        Predicate<Artifact> filter = c -> c != mockComponents.get(3);
        Optional<Artifact> optional = artifacts.hierarchy(filter).parent(URI.create("/abc/"));
        assertNotNull(optional);
        assertFalse(optional.isPresent());

        //no parent for a top component
        optional = artifacts.hierarchy(filter).parent(URI.create("/domain1/"));
        assertNotNull(optional);
        assertFalse(optional.isPresent());

        optional = artifacts.hierarchy(filter).parent(URI.create("/domain1/sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockComponents.get(0), optional.get());

        optional = artifacts.hierarchy(filter).parent(URI.create("/domain1/sub1/sub-sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockComponents.get(0), optional.get());
    }

    @Test
    public void children() throws Exception {
        Set<Artifact> children = artifacts.hierarchy().children(URI.create("/")).collect(Collectors.toSet());
        assertEquals(5, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockComponents.get(0),
                mockComponents.get(1),
                mockComponents.get(2),
                mockComponents.get(7),
                mockComponents.get(8)
        )));

        children = artifacts.hierarchy().children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockComponents.get(3)));

        children = artifacts.hierarchy().children(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(2, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockComponents.get(5),
                mockComponents.get(9)
        )));

        children = artifacts.hierarchy().children(URI.create("/xyz/")).collect(Collectors.toSet());
        assertEquals(children, Collections.emptySet());
    }

    @Test
    public void childrenWithFilter() throws Exception {
        Set<Artifact> children = artifacts.hierarchy(
                c -> mockComponents.get(0) != c
        ).children(URI.create("/")).collect(Collectors.toSet());
        assertEquals(5, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockComponents.get(3),
                mockComponents.get(1),
                mockComponents.get(2),
                mockComponents.get(7),
                mockComponents.get(8)
        )));

        children = artifacts.hierarchy(
                c -> mockComponents.get(0) != c
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockComponents.get(3)));

        children = artifacts.hierarchy(
                c -> !c.getIdentifier().toString().startsWith("/domain1/")
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.emptySet());

        children = artifacts.hierarchy(
                c -> mockComponents.get(3) != c
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(2, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockComponents.get(5),
                mockComponents.get(9)
        )));

        children = artifacts.hierarchy(
                c -> mockComponents.get(5) != c
        ).children(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockComponents.get(9)));
    }

}