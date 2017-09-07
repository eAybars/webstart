package net.novalab.webstart.service.artifact.boundary;

import jnlp.sample.servlet.JnlpDownloadServlet;
import net.novalab.webstart.service.artifact.control.Artifacts;
import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.filter.entity.AggregatedFilter;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;
import net.novalab.webstart.service.json.control.Pagination;
import net.novalab.webstart.service.json.entity.JsonErrorResponse;
import net.novalab.webstart.service.json.entity.JsonSerializable;
import net.novalab.webstart.service.uri.control.URIBuilder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by ertunc on 30/05/17.
 */
@Path("artifact")
@Stateless
@Produces("application/json;charset=utf-8")
public class ArtifactService {
    @Inject
    Artifacts artifacts;
    @Inject
    @AggregatedFilter
    @VisibilityFilter
    Predicate<Artifact> filter;

    @GET
    @Path("home")
    @Produces("text/plain")
    public String getHome(@Context HttpServletRequest request) {
        return request.getContextPath() + JnlpDownloadServlet.PATH;
    }

    @GET
    public JsonObject getAll(@QueryParam("start") @DefaultValue("0") @Min(0) int start,
                             @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        return Pagination.of
                (artifacts.stream()
                        .filter(filter)
                        .sorted()
                        .collect(Collectors.toList())
                ).startingFrom(start).withSize(size).done();
    }

    @GET
    @Path("parent/{segments: .+}")
    public Response getParent(@PathParam("segments") List<PathSegment> segments) {
        try {
            return artifacts.hierarchy(filter).parent(URIBuilder.from(segments).addPathFromSource().build())
                    .map(JsonSerializable::toJson)
                    .map(Response::ok)
                    .orElse(Response.status(Response.Status.NOT_FOUND))
                    .build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("children/{segments: .*}")
    public Response getChildren(@PathParam("segments") List<PathSegment> segments,
                                @QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        try {
            return Response.ok(Pagination.of(
                    artifacts.hierarchy(filter)
                            .children(URIBuilder.from(segments).addPathFromSource().build())
                            .sorted()
                            .collect(Collectors.toList())
                    ).startingFrom(start).withSize(size).done()
            ).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

}
