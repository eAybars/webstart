package com.eaybars.webstart.service.discovery.boundary;

import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.backend.control.Backends;
import com.eaybars.webstart.service.filter.entity.AggregatedFilter;
import com.eaybars.webstart.service.filter.entity.VisibilityFilter;
import com.eaybars.webstart.service.json.control.Pagination;
import com.eaybars.webstart.service.json.entity.JsonErrorResponse;
import com.eaybars.webstart.service.uri.control.URIBuilder;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.discovery.control.BackendArtifactSupplier;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Stateless
@Path("artifact/backend")
@Produces("application/json;charset=utf-8")
//@RolesAllowed("Admin")
public class BackendArtifactService {
    @Inject
    BackendArtifactSupplier artifactSupplier;

    @Inject
    Backends backends;

    @Inject
    @AggregatedFilter
    @VisibilityFilter
    Predicate<Artifact> filter;

    @GET
    @Path("{segments: .+}")
    public JsonObject getBackendArtifacts(@PathParam("segments") List<PathSegment> segments,
            @QueryParam("start") @DefaultValue("0") @Min(0) int start,
                             @QueryParam("size") @DefaultValue("100") @Min(0) int size) throws URISyntaxException {
        return Pagination.of
                (artifactSupplier.getBackendArtifacts(URIBuilder.from(segments).addPathFromSource().build())
                        .filter(filter)
                        .sorted()
                        .collect(Collectors.toList())
                ).startingFrom(start).withSize(size).done();
    }

    @POST
    @Path("reload")
    public JsonObject reload() {
        return artifactSupplier.reloadAll().toJson();
    }

    @POST
    @Path("reload/{segments: .+}")
    public JsonObject reload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactSupplier.reload(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("update/")
    public JsonObject update() {
        return artifactSupplier.updateAll().toJson();
    }

    @POST
    @Path("update/{segments: .+}")
    public JsonObject update(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactSupplier.update(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("unload")
    public JsonObject unload() {
        return artifactSupplier.unloadAll().toJson();
    }

    @POST
    @Path("unload/{segments: .+}")
    public JsonObject unload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException, IOException {
        return artifactSupplier.unload(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @DELETE
    @Path("{segments: .+}")
    public JsonObject delete(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException, IOException {
        URI target = URIBuilder.from(segments).addPathFromSource().build();
        Backends.BackendURI backendURI = getBackendURI(target);
        if (backendURI.getBackend().getStorage().get().delete(backendURI.getUri())) {
            return artifactSupplier.unload(target).toJson();
        } else {
            return new ArtifactEventSummary().toJson();
        }
    }

    @PUT
    @Path("{segments: .+}")
    @Consumes("application/octet-stream")
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public JsonObject put(@PathParam("segments") List<PathSegment> segments, InputStream is) throws URISyntaxException, IOException {
        URI target = URIBuilder.from(segments).addPathFromSource().build();
        Backends.BackendURI backendURI = getBackendURI(target);
        if (backendURI.isDirectory()
                ? backendURI.getBackend().getStorage().get().storeZip(backendURI.getUri(), is)
                : backendURI.getBackend().getStorage().get().store(backendURI.getUri(), is)) {
            URI reloadTarget = backendURI.isDirectory()
                    ? target
                    : URIBuilder.from(target).addParentPathFromSource().addPath("/").build();
            return artifactSupplier.update(reloadTarget).toJson();
        } else {
            return new ArtifactEventSummary().toJson();
        }
    }

    private Backends.BackendURI getBackendURI(URI target) {
        Optional<Backends.BackendURI> backendURIOptional = backends.toBackendURI(target);
        if (backendURIOptional.isPresent()) {
            Backends.BackendURI backendURI = backendURIOptional.get();
            if (!backendURI.getBackend().getStorage().isPresent()) {
                throw new NotImplementedException();
            } else {
                return backendURI;
            }
        } else {
            throw new NotFoundException(Response.status(Response.Status.NOT_FOUND).entity(
                    new JsonErrorResponse("No backend found to handle request uri " + target).toJson()
            ).build());
        }

    }
}