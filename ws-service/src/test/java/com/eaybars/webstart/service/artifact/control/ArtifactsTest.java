package com.eaybars.webstart.service.artifact.control;

import com.eaybars.webstart.service.artifact.MockArtufactRule;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ArtifactsTest {
    @Rule
    public MockArtufactRule mockArtifacts = new MockArtufactRule(Stream.of(
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
        artifacts.cache = mockArtifacts.getArtifacts().stream().collect(Collectors.toMap(Artifact::getIdentifier, Function.identity()));
    }

    @Test
    public void stream() throws Exception {
        assertEquals(10, artifacts.stream().count());
        assertTrue(artifacts.stream().collect(Collectors.toSet()).containsAll(mockArtifacts.getArtifacts()));
    }

    @Test
    public void top() throws Exception {
        Set<? extends Artifact> set = artifacts.hierarchy().top().collect(Collectors.toSet());
        assertEquals(5, set.size());
        assertTrue(set.containsAll(Arrays.asList(
                mockArtifacts.get(0),
                mockArtifacts.get(1),
                mockArtifacts.get(2),
                mockArtifacts.get(7),
                mockArtifacts.get(8)
        )));
    }

    @Test
    public void topWithFilter() throws Exception {
        Set<? extends Artifact> set = artifacts.hierarchy(
                c -> mockArtifacts.get(2) != c && mockArtifacts.get(9) != c
        ).top().collect(Collectors.toSet());
        assertEquals(5, set.size());
        assertTrue(set.containsAll(Arrays.asList(
                mockArtifacts.get(0),
                mockArtifacts.get(1),
                mockArtifacts.get(4),
                mockArtifacts.get(7),
                mockArtifacts.get(8)
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
        assertSame(mockArtifacts.get(0), optional.get());

        optional = artifacts.hierarchy().parent(URI.create("/domain1/xyz/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockArtifacts.get(0), optional.get());

        optional = artifacts.hierarchy().parent(URI.create("/domain1/sub1/sub-sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockArtifacts.get(3), optional.get());
    }

    @Test
    public void parentWithFilter() throws Exception {
        Predicate<Artifact> filter = c -> c != mockArtifacts.get(3);
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
        assertSame(mockArtifacts.get(0), optional.get());

        optional = artifacts.hierarchy(filter).parent(URI.create("/domain1/sub1/sub-sub1/"));
        assertNotNull(optional);
        assertTrue(optional.isPresent());
        assertSame(mockArtifacts.get(0), optional.get());
    }

    @Test
    public void children() throws Exception {
        Set<Artifact> children = artifacts.hierarchy().children(URI.create("/")).collect(Collectors.toSet());
        assertEquals(5, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockArtifacts.get(0),
                mockArtifacts.get(1),
                mockArtifacts.get(2),
                mockArtifacts.get(7),
                mockArtifacts.get(8)
        )));

        children = artifacts.hierarchy().children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockArtifacts.get(3)));

        children = artifacts.hierarchy().children(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(2, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        children = artifacts.hierarchy().children(URI.create("/xyz/")).collect(Collectors.toSet());
        assertEquals(children, Collections.emptySet());
    }

    @Test
    public void childrenWithFilter() throws Exception {
        Set<Artifact> children = artifacts.hierarchy(
                c -> mockArtifacts.get(0) != c
        ).children(URI.create("/")).collect(Collectors.toSet());
        assertEquals(5, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockArtifacts.get(3),
                mockArtifacts.get(1),
                mockArtifacts.get(2),
                mockArtifacts.get(7),
                mockArtifacts.get(8)
        )));

        children = artifacts.hierarchy(
                c -> mockArtifacts.get(0) != c
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockArtifacts.get(3)));

        children = artifacts.hierarchy(
                c -> !c.getIdentifier().toString().startsWith("/domain1/")
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.emptySet());

        children = artifacts.hierarchy(
                c -> mockArtifacts.get(3) != c
        ).children(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(2, children.size());
        assertTrue(children.containsAll(Arrays.asList(
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        children = artifacts.hierarchy(
                c -> mockArtifacts.get(5) != c
        ).children(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(children, Collections.singleton(mockArtifacts.get(9)));
    }

    @Test
    public void descendants() throws Exception {
        Set<Artifact> descendants = artifacts.hierarchy().descendants(URI.create("/")).collect(Collectors.toSet());
        assertEquals(10, descendants.size());

        descendants = artifacts.hierarchy().descendants(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(4, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(
                mockArtifacts.get(0),
                mockArtifacts.get(3),
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        descendants = artifacts.hierarchy().descendants(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(3, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(
                mockArtifacts.get(3),
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        descendants = artifacts.hierarchy().descendants(URI.create("/xyz/")).collect(Collectors.toSet());
        assertEquals(descendants, Collections.emptySet());
    }

    @Test
    public void descendantsWithFilter() throws Exception {
        Set<Artifact> descendants = artifacts.hierarchy(
                c -> mockArtifacts.get(0) != c
        ).descendants(URI.create("/")).collect(Collectors.toSet());
        assertEquals(9, descendants.size());
        assertFalse(descendants.contains(mockArtifacts.get(0)));

        descendants = artifacts.hierarchy(
                c -> mockArtifacts.get(0) != c
        ).descendants(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(3, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(
                mockArtifacts.get(3),
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        descendants = artifacts.hierarchy(
                c -> !c.getIdentifier().toString().startsWith("/domain1/")
        ).descendants(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(descendants, Collections.emptySet());

        descendants = artifacts.hierarchy(
                c -> mockArtifacts.get(3) != c
        ).descendants(URI.create("/domain1/")).collect(Collectors.toSet());
        assertEquals(3, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(
                mockArtifacts.get(0),
                mockArtifacts.get(5),
                mockArtifacts.get(9)
        )));

        descendants = artifacts.hierarchy(
                c -> mockArtifacts.get(5) != c
        ).descendants(URI.create("/domain1/sub1/")).collect(Collectors.toSet());
        assertEquals(2, descendants.size());
        assertTrue(descendants.containsAll(Arrays.asList(
                mockArtifacts.get(3),
                mockArtifacts.get(9)
        )));
    }

}