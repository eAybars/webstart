package com.eaybars.webstart.service.backend.boundary;

import com.eaybars.webstart.service.artifact.entity.ArtifactEventSummary;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.BackendArtifacts;
import com.eaybars.webstart.service.backend.control.Storage;
import com.eaybars.webstart.service.json.entity.JsonErrorResponse;
import com.eaybars.webstart.service.uri.control.URIBuilder;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Path("artifact/backend")
@Produces("application/json;charset=utf-8")
public class BackendService {

    private static final Logger LOGGER = Logger.getLogger(BackendService.class.getName());

    @Inject
    Backend backend;
    @Inject
    BackendArtifacts backendArtifacts;

    @POST
    @Path("reload")
    public JsonObject reloadAll() {
        return backendArtifacts.reloadAll().toJson();
    }

    @POST
    @Path("reload/{segments: .+}")
    public JsonObject reload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return backendArtifacts.reload(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("update/")
    public JsonObject updateAll() {
        return backendArtifacts.updateAll().toJson();
    }

    @POST
    @Path("update/{segments: .+}")
    public JsonObject update(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return backendArtifacts.update(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("unload")
    public JsonObject unloadAll() {
        return backendArtifacts.unloadAll().toJson();
    }

    @POST
    @Path("unload/{segments: .+}")
    public JsonObject unload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException, IOException {
        return backendArtifacts.unload(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("load")
    public JsonObject loadAll() {
        return backendArtifacts.loadAll().toJson();
    }

    @POST
    @Path("load/{segments: .+}")
    public JsonObject load(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException, IOException {
        return backendArtifacts.load(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @DELETE
    @Path("{segments: .+}")
    public JsonObject delete(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        URI target = URIBuilder.from(segments).addPathFromSource().build();
        if (backend.getStorage().isPresent()) {
            Storage storage = backend.getStorage().get();
            try {
                if (storage.delete(target)) {
                    return backendArtifacts.unload(target).toJson();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An error occured while deleting the target " + target, e);
                throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new JsonErrorResponse(e.getLocalizedMessage(), "An error occured while deleting the target " + target).toJson()
                ).build());
            }
            return ArtifactEventSummary.EMPTY.toJson();
        } else {
            throw new NotImplementedException();
        }
    }

    @PUT
    @Path("{segments: .+}")
    @Consumes("application/octet-stream")
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public JsonObject put(@PathParam("segments") List<PathSegment> segments, InputStream is) throws URISyntaxException {
        URI target = URIBuilder.from(segments).addPathFromSource().build();
        if (backend.getStorage().isPresent()) {
            Storage storage = backend.getStorage().get();
            try {
                if (backend.isDirectory(target)) {
                    if (storage.storeZip(target, is))
                        return backendArtifacts.update(target).toJson();
                } else if (storage.store(target, is)) {
                    return backendArtifacts.update(URIBuilder.from(target).addParentPathFromSource().addPath("/").build()).toJson();
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "An error occured while creating contents for target " + target, e);
                throw new ServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new JsonErrorResponse(e.getLocalizedMessage(), "An error occured while creating contents for target " + target).toJson()
                ).build());
            }
            return ArtifactEventSummary.EMPTY.toJson();
        } else {
            throw new NotImplementedException();
        }
    }

}
