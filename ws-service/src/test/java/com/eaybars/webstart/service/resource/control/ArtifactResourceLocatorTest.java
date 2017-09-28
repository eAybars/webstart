package com.eaybars.webstart.service.resource.control;

import com.eaybars.webstart.service.artifact.MockComponentRule;
import com.eaybars.webstart.service.artifact.control.Artifacts;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArtifactResourceLocatorTest {
    @Rule
    public MockComponentRule mockComponentRule = new MockComponentRule(Stream.of(
            "/domain1/",
            "/something/",              //1
            "/domain2/",
            "/domain1/sub1/",           //3
            "/domain2/sub1/",
            "/domain1/sub1/sub-sub1/",  //5
            "/domain2/sub1/sub-sub1/",
            "/other/something/",        //7
            "/sub1/domain2/sub-sub1/",
            "/domain1/sub1/sub-sub2/"   //9
    ));

    private ArtifactResourceLocator resourceLocator;

    private Artifacts artifacts;

    @Before
    public void setUp() throws URISyntaxException {
        artifacts = mock(Artifacts.class);

        when(artifacts.stream()).thenAnswer(a -> mockComponentRule.getComponents().stream());

        resourceLocator = new ArtifactResourceLocator();
        resourceLocator.artifacts = artifacts;
    }


    @Test
    public void applyWithNoFilter() throws Exception {
        resourceLocator.accessFilter = c -> true;

        resourceLocator.apply("/missing/resource.png");
        mockComponentRule.getComponents().forEach(c -> verify(c, times(0)).getResource(anyString()));
        resourceLocator.apply("/domain1/sub1/sub-sub1/someresource.jar");
        mockComponentRule.getComponents().stream()
                .filter(c -> !c.equals(mockComponentRule.get(5)))
                .forEach(c -> verify(c, times(0)).getResource(anyString()));
        verify(mockComponentRule.get(5)).getResource("someresource.jar");
        resourceLocator.apply("/domain2/sub1/someresource2.jar");
        verify(mockComponentRule.get(4)).getResource("someresource2.jar");
    }

    @Test
    public void applyWithMiddleTierDomainFiltered() throws Exception {
        resourceLocator.accessFilter = c -> c != mockComponentRule.get(3);

        resourceLocator.apply("/domain1/resource1.png");
        verify(mockComponentRule.get(0)).getResource("resource1.png");
        verify(mockComponentRule.get(5), times(0)).getResource(anyString());
        resourceLocator.apply("/domain1/sub1/resource2.png");
        verify(mockComponentRule.get(3), times(0)).getResource(anyString());
        verify(mockComponentRule.get(5), times(0)).getResource(anyString());
        resourceLocator.apply("/domain1/sub1/sub-sub1/resource3.png");
        verify(mockComponentRule.get(5)).getResource("resource3.png");
    }

    @Test
    public void toArtifactAndPath() throws Exception {
        resourceLocator.accessFilter = c -> true;

        Optional<ArtifactResourceLocator.ArtifactResource> resource = resourceLocator.toArtifactResource("/domain1/sub1/sub-sub2/resource.png");
        assertNotNull(resource);
        assertTrue(resource.isPresent());
        assertSame(mockComponentRule.get(9), resource.get().getArtifact());
        assertEquals("resource.png", resource.get().getPath());

        resource = resourceLocator.toArtifactResource("/domain1/sub1/resource.png");
        assertNotNull(resource);
        assertTrue(resource.isPresent());
        assertSame(mockComponentRule.get(3), resource.get().getArtifact());
        assertEquals("resource.png", resource.get().getPath());

        resource = resourceLocator.toArtifactResource("/domain1/sub2/resource.png");
        assertNotNull(resource);
        assertTrue(resource.isPresent());
        assertSame(mockComponentRule.get(0), resource.get().getArtifact());
        assertEquals("sub2/resource.png", resource.get().getPath());

        resource = resourceLocator.toArtifactResource("/domain3/sub2/resource.png");
        assertNotNull(resource);
        assertFalse(resource.isPresent());
    }

    @Test
    public void componentForResourceWithFilter() throws Exception {
        resourceLocator.accessFilter = c -> c != mockComponentRule.get(9);

        Optional<ArtifactResourceLocator.ArtifactResource> resource = resourceLocator.toArtifactResource("/domain1/sub1/sub-sub2/resource.png");
        assertNotNull(resource);
        assertFalse(resource.isPresent());

        resource = resourceLocator.toArtifactResource("/domain1/sub1/resource.png");
        assertNotNull(resource);
        assertTrue(resource.isPresent());
        assertSame(mockComponentRule.get(3), resource.get().getArtifact());
        assertEquals("resource.png", resource.get().getPath());

        resource = resourceLocator.toArtifactResource("/domain1/sub2/resource.png");
        assertNotNull(resource);
        assertTrue(resource.isPresent());
        assertSame(mockComponentRule.get(0), resource.get().getArtifact());
        assertEquals("sub2/resource.png", resource.get().getPath());

        resource = resourceLocator.toArtifactResource("/domain3/sub2/resource.png");
        assertNotNull(resource);
        assertFalse(resource.isPresent());

    }

}