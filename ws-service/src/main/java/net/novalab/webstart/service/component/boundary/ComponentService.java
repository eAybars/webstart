package net.novalab.webstart.service.component.boundary;

import com.sun.jndi.toolkit.url.Uri;
import jnlp.sample.servlet.JnlpDownloadServlet;
import net.novalab.webstart.service.component.control.Components;
import net.novalab.webstart.service.component.entity.Component;
import net.novalab.webstart.service.json.control.Pagination;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ertunc on 30/05/17.
 */
@Path("component")
@Stateless
@Produces("application/json;charset=utf-8")
public class ComponentService {
    @Inject
    Components components;

    @GET
    @Path("home")
    @Produces("text/plain")
    public String getComponentHome(@Context  HttpServletRequest request) {
        return request.getContextPath()+ JnlpDownloadServlet.PATH;
    }

    @GET
    public JsonObject getAllComponents(@QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                       @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        return Pagination.of(findImmediateComponents(components.filtered()))
                .startingFrom(start)
                .withSize(size)
                .done();
    }

    @GET
    @Path("parent")
    public Response getParent(@QueryParam("component") String componentId) {
        try {
            URI identifier = new URI(Optional.ofNullable(componentId)
                    .map(c -> c.endsWith("/") ? c : c+"/")
                    .orElse(null));
            return components.filtered()
                    .filter(c -> identifier.toString().startsWith(c.getIdentifier().toString()))
                    .sorted(Comparator.reverseOrder())
                    .findFirst()
                    .map(Response::ok)
                    .orElse(Response.status(Response.Status.NOT_FOUND))
                    .build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(componentId + " is not a valid URI").build();
        } catch (NullPointerException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Parent component identifier must be specified").build();
        }
    }

    @GET
    @Path("children")
    public JsonObject getChildren(@QueryParam("parent") @DefaultValue(".*") String parentIdentifier,
                                  @QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                  @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        return Pagination.of(findImmediateComponents(components.filtered()
                .filter(c -> c.getIdentifier().toString().matches(parentIdentifier))))
                .startingFrom(start)
                .withSize(size)
                .done();
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
