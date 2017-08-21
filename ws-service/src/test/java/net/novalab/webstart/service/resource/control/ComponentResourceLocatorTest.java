package net.novalab.webstart.service.resource.control;

import net.novalab.webstart.service.component.MockComponentFilter;
import net.novalab.webstart.service.component.MockComponentRule;
import net.novalab.webstart.service.component.control.Components;
import net.novalab.webstart.service.component.entity.Component;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ComponentResourceLocatorTest {
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

    private ComponentResourceLocator resourceLocator;

    private Components components;

    @Before
    public void setUp() throws URISyntaxException {
        components = mock(Components.class);

        when(components.stream()).thenAnswer(a -> mockComponentRule.getComponents().stream());

        resourceLocator = new ComponentResourceLocator();
        resourceLocator.components = components;
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
    public void componentForResourceWithoutFilter() throws Exception {
        resourceLocator.accessFilter = c -> true;

        Optional<Component> component = resourceLocator.componentForResource("/domain1/sub1/sub-sub2/resource.png");
        assertNotNull(component);
        assertTrue(component.isPresent());
        assertSame(mockComponentRule.get(9), component.get());

        component = resourceLocator.componentForResource("/domain1/sub1/resource.png");
        assertNotNull(component);
        assertTrue(component.isPresent());
        assertSame(mockComponentRule.get(3), component.get());

        component = resourceLocator.componentForResource("/domain1/sub2/resource.png");
        assertNotNull(component);
        assertTrue(component.isPresent());
        assertSame(mockComponentRule.get(0), component.get());

        component = resourceLocator.componentForResource("/domain3/sub2/resource.png");
        assertNotNull(component);
        assertFalse(component.isPresent());
    }

    @Test
    public void componentForResourceWithFilter() throws Exception {
        resourceLocator.accessFilter = c -> c != mockComponentRule.get(9);

        Optional<Component> component = resourceLocator.componentForResource("/domain1/sub1/sub-sub2/resource.png");
        assertNotNull(component);
        assertFalse(component.isPresent());

        component = resourceLocator.componentForResource("/domain1/sub1/resource.png");
        assertNotNull(component);
        assertTrue(component.isPresent());
        assertSame(mockComponentRule.get(3), component.get());

        component = resourceLocator.componentForResource("/domain1/sub2/resource.png");
        assertNotNull(component);
        assertTrue(component.isPresent());
        assertSame(mockComponentRule.get(0), component.get());

        component = resourceLocator.componentForResource("/domain3/sub2/resource.png");
        assertNotNull(component);
        assertFalse(component.isPresent());

    }

}