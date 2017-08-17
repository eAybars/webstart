package net.novalab.webstart.service.component.control;

import net.novalab.webstart.service.component.entity.Component;
import org.junit.Before;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

public class ComponentsTest {

    private Components components;
    private List<Component> mockComponents;

    @Before
    public void setUp() throws URISyntaxException {
        components = new Components();
        mockComponents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Component c = mock(Component.class);
            when(c.toString()).thenReturn("c" + (i + 1));
            mockComponents.add(c);
        }
        when(mockComponents.get(0).getIdentifier()).thenReturn(new URI("/domain1/"));
        when(mockComponents.get(3).getIdentifier()).thenReturn(new URI("/domain1/sub1/"));
        when(mockComponents.get(5).getIdentifier()).thenReturn(new URI("/domain1/sub1/sub-sub1/"));

        ComponentSupplier s1 = mock(ComponentSupplier.class);
        when(s1.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(0),
                mockComponents.get(3),
                mockComponents.get(5))
        );

        when(mockComponents.get(2).getIdentifier()).thenReturn(new URI("/domain2/"));
        when(mockComponents.get(4).getIdentifier()).thenReturn(new URI("/domain2/sub1/"));
        when(mockComponents.get(6).getIdentifier()).thenReturn(new URI("/domain2/sub1/sub-sub1/"));

        ComponentSupplier s2 = mock(ComponentSupplier.class);
        when(s2.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(2),
                mockComponents.get(4),
                mockComponents.get(6))
        );


        when(mockComponents.get(1).getIdentifier()).thenReturn(new URI("/something/"));
        when(mockComponents.get(9).getIdentifier()).thenReturn(new URI("/filtered/"));
        when(mockComponents.get(7).getIdentifier()).thenReturn(new URI("/other/something/"));
        when(mockComponents.get(8).getIdentifier()).thenReturn(new URI("/sub1/domain2/sub-sub1/"));

        ComponentSupplier s3 = mock(ComponentSupplier.class);
        when(s3.get()).thenAnswer(a -> Stream.of(
                mockComponents.get(1),
                mockComponents.get(9),
                mockComponents.get(7),
                mockComponents.get(8))
        );

        components.componentSuppliers = mock(Instance.class);
        when(components.componentSuppliers.spliterator()).thenAnswer(a -> Arrays.asList(s1, s2, s3).spliterator());

        components.componentFilters = mock(Instance.class);

        Predicate<Component> f1 = c -> !c.equals(mockComponents.get(4));
        Predicate<Component> f2 = c -> !c.equals(mockComponents.get(9));
        when(components.componentFilters.spliterator()).thenAnswer(a -> Arrays.asList(f1, f2).spliterator());

    }

    @Test
    public void all() throws Exception {
        assertEquals(10, components.all().count());
        assertTrue(components.all().collect(Collectors.toSet()).containsAll(mockComponents));
    }

    @Test
    public void filtered() throws Exception {
        assertEquals(8, components.filtered().count());
        Set<Component> filtered = components.filtered().collect(Collectors.toSet());
        assertFalse(filtered.contains(mockComponents.get(4)));
        assertFalse(filtered.contains(mockComponents.get(9)));
    }

    @Test
    public void test1() throws Exception {
        assertTrue(components.test(mockComponents.get(0)));
        assertTrue(components.test(mockComponents.get(1)));
        assertTrue(components.test(mockComponents.get(2)));
        assertTrue(components.test(mockComponents.get(3)));
        assertFalse(components.test(mockComponents.get(4)));
        assertTrue(components.test(mockComponents.get(5)));
        assertTrue(components.test(mockComponents.get(6)));
        assertTrue(components.test(mockComponents.get(7)));
        assertTrue(components.test(mockComponents.get(8)));
        assertFalse(components.test(mockComponents.get(9)));
    }

}