package net.novalab.webstart.google.artifact.boundary;

import net.novalab.webstart.google.artifact.control.CloudStorageArtifactSupplier;
import net.novalab.webstart.service.uri.control.URIBuilder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.PathSegment;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Stateless
@Path("artifact/gcs")
@Produces("application/json;charset=utf-8")
//@RolesAllowed("Admin")
public class CloudStorageBasedArtifactService {
    @Inject
    CloudStorageArtifactSupplier artifactSupplier;

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
}
