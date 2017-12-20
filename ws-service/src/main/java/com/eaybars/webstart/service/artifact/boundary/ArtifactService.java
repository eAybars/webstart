package com.eaybars.webstart.service.artifact.boundary;

import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.filter.entity.AggregatedFilter;
import com.eaybars.webstart.service.filter.entity.VisibilityFilter;
import com.eaybars.webstart.service.json.control.Pagination;
import com.eaybars.webstart.service.json.entity.JsonSerializable;
import com.eaybars.webstart.service.uri.control.URIBuilder;
import jnlp.sample.resource.ResourceLocator;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
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
        return request.getContextPath() + ResourceLocator.PATH;
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
    @Path("id/{segments: .+}")
    public JsonObject getArtifact(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        URI id = URIBuilder.from(segments).addPathFromSource().build();
        return artifacts.stream()
                .filter(filter)
                .filter(a -> a.getIdentifier().equals(id))
                .map(Artifact::toJson)
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("parent/{segments: .+}")
    public JsonObject getParent(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifacts.hierarchy(filter)
                .parent(URIBuilder.from(segments).addPathFromSource().build())
                .map(JsonSerializable::toJson)
                .orElseThrow(NotFoundException::new);
    }

    @GET
    @Path("children")
    public JsonObject getChildren(@QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                  @QueryParam("size") @DefaultValue("100") @Min(0) int size) {
        return Pagination.of(
                artifacts.hierarchy(filter)
                        .children(URI.create("/"))
                        .sorted()
                        .collect(Collectors.toList())
        ).startingFrom(start).withSize(size).done();
    }

    @GET
    @Path("children/{segments: .*}")
    public JsonObject getChildren(@PathParam("segments") List<PathSegment> segments,
                                  @QueryParam("start") @DefaultValue("0") @Min(0) int start,
                                  @QueryParam("size") @DefaultValue("100") @Min(0) int size) throws URISyntaxException {
        return Pagination.of(
                artifacts.hierarchy(filter)
                        .children(URIBuilder.from(segments).addPathFromSource().build())
                        .sorted()
                        .collect(Collectors.toList())
        ).startingFrom(start).withSize(size).done();
    }

}
