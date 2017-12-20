package com.eaybars.webstart.service.resource.control;

import com.eaybars.webstart.service.artifact.MockArtufactRule;
import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.backend.control.Backend;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static com.eaybars.webstart.service.resource.control.ArtifactResourceLocatorTest.URIParentMatcher.of;
import static org.mockito.Mockito.*;

public class ArtifactResourceLocatorTest {
    @Rule
    public MockArtufactRule mockArtufactRule = new MockArtufactRule(Stream.of(
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

    private Backend backend;
    private Artifacts artifacts;

    @Before
    public void setUp() throws URISyntaxException {
        artifacts = mock(Artifacts.class);
        Artifacts.Hierarchy hierarchy = mock(Artifacts.Hierarchy.class);
        when(artifacts.hierarchy()).thenReturn(hierarchy);

        when(hierarchy.parents(any())).thenAnswer(a -> Stream.empty());
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(0).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(0)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(1).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(1)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(2).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(2)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(3).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(0),
                        mockArtufactRule.get(3)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(4).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(2),
                        mockArtufactRule.get(4)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(5).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(0),
                        mockArtufactRule.get(3),
                        mockArtufactRule.get(5)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(6).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(2),
                        mockArtufactRule.get(4),
                        mockArtufactRule.get(6)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(7).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(7)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(8).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(8)
                ));
        when(hierarchy.parents(argThat(of(mockArtufactRule.get(9).getIdentifier()))))
                .thenAnswer(a -> Stream.of(
                        mockArtufactRule.get(0),
                        mockArtufactRule.get(3),
                        mockArtufactRule.get(9)
                ));

        when(artifacts.stream()).thenAnswer(a -> mockArtufactRule.getArtifacts().stream());

        resourceLocator = new ArtifactResourceLocator();
        resourceLocator.artifacts = artifacts;
        resourceLocator.backend = backend = mock(Backend.class);
    }


    @Test
    public void applyWithNoFilter() throws Exception {
        resourceLocator.accessFilter = c -> true;

        resourceLocator.apply("/missing/resource.png");
        verify(backend, times(0)).getResource(any());

        URI uri = URI.create("/domain1/sub1/sub-sub1/someresource.jar");
        resourceLocator.apply(uri.toString());
        verify(backend, times(1)).getResource(uri);

        uri = URI.create("/domain2/sub1/someresource2.jar");
        resourceLocator.apply(uri.toString());
        verify(backend).getResource(uri);
    }

    @Test
    public void applyWithMiddleTierDomainFiltered() throws Exception {
        resourceLocator.accessFilter = c -> c != mockArtufactRule.get(3);

        URI uri = URI.create("/domain1/resource1.png");
        resourceLocator.apply(uri.toString());
        verify(backend).getResource(uri);

        uri = URI.create("/domain1/sub1/resource2.png");
        resourceLocator.apply(uri.toString());
        verify(backend, times(0)).getResource(uri);

        uri = URI.create("/domain1/sub1/sub-sub1/resource3.png");
        resourceLocator.apply(uri.toString());
        verify(backend).getResource(uri);

    }

    public static class URIParentMatcher extends ArgumentMatcher<URI> {
        private URI source;

        public static URIParentMatcher of(URI source) {
            return new URIParentMatcher(source);
        }

        public URIParentMatcher(URI source) {
            this.source = source;
        }

        @Override
        public boolean matches(Object argument) {
            return argument != null && argument.toString().startsWith(source.toString());
        }
    }

}