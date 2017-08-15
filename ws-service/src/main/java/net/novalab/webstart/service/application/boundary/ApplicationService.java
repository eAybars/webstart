package net.novalab.webstart.service.application.boundary;

import net.novalab.webstart.service.application.controller.Components;
import net.novalab.webstart.service.application.entity.Component;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Created by ertunc on 30/05/17.
 */
@Path("application")
@Stateless
public class ApplicationService {
    @Inject
    Components components;

    @GET
    public List<Component> getAllComponents(@QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                            @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        List<Component> list = findImmediateComponents(components.filtered());
        return list.subList(Math.min(start, list.size()), Math.min(start + size, list.size()));
    }

    @GET
    @Path("parent")
    public Component getParent(@QueryParam("component") String componentId) {
        return null;
    }

    @GET
    @Path("children")
    public List<Component> getChildren(@QueryParam("parent") @DefaultValue(".*") String parentIdentifier,
                                       @QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                       @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        List<Component> list = findImmediateComponents(components.filtered()
                .filter(c -> c.getIdentifier().toString().matches(parentIdentifier)));
        return list.subList(Math.min(start, list.size()), Math.min(start + size, list.size()));
    }


    private List<Component> findImmediateComponents(Stream<Component> components) {
        return new ArrayList<>(components
                .reduce(new TreeMap<>(), this::updateMap, (m1, m2) -> {
                    TreeMap<String, Component> map = new TreeMap<>(m1);
                    m2.values().forEach(c -> updateMap(map, c));
                    return map;
                }).values());
    }

    private TreeMap<String, Component> updateMap(TreeMap<String, Component> m, Component c) {
        if (m.headMap(c.getIdentifier().toString()).isEmpty()) {
            m.tailMap(c.getIdentifier().toString());
            m.put(c.getIdentifier().toString(), c);
        }
        return m;
    }

}
