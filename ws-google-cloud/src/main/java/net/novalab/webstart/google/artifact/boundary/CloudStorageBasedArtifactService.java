package net.novalab.webstart.google.artifact.boundary;

import net.novalab.webstart.google.artifact.control.CloudStorageArtifactSupplier;
import net.novalab.webstart.google.storage.control.ArtifactStorage;
import net.novalab.webstart.service.uri.control.URIBuilder;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Stateless
@Path("artifact/gcs")
//@RolesAllowed("Admin")
public class CloudStorageBasedArtifactService {
    @Inject
    CloudStorageArtifactSupplier artifactSupplier;
    @Inject
    ArtifactStorage artifactStorage;

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
        return artifactSupplier.update(URI.create("/")).toJson();
    }

    @POST
    @Path("update/{segments: .+}")
    public JsonObject update(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactSupplier.update(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @POST
    @Path("unload")
    public JsonObject unload() {
        return artifactSupplier.unload(URI.create("/")).toJson();
    }

    @POST
    @Path("unload/{segments: .+}")
    public JsonObject unload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactSupplier.unload(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @DELETE
    @Path("store/{segments: .+}")
    public JsonObject delete(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactStorage.delete(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @PUT
    @Path("store/{segments: .+}")
    public JsonObject put(@PathParam("segments") List<PathSegment> segments, InputStream is) throws URISyntaxException, IOException {
        return artifactStorage.put(URIBuilder.from(segments).addPathFromSource().build(), is).toJson();
    }
}
