package net.novalab.webstart.google.storage.boundary;

import net.novalab.webstart.google.storage.control.ArtifactStorage;
import net.novalab.webstart.service.uri.control.URIBuilder;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.PathSegment;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

@Stateless
@Path("artifact/store/gcs/")
@Produces("application/json;charset=utf-8")
//@RolesAllowed("Admin")
public class StorageService {
    @Inject
    ArtifactStorage artifactStorage;

    @DELETE
    @Path("{segments: .+}")
    public JsonObject delete(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        return artifactStorage.delete(URIBuilder.from(segments).addPathFromSource().build()).toJson();
    }

    @PUT
    @Path("{segments: .+}")
    @Consumes("application/octet-stream")
    public JsonObject put(@PathParam("segments") List<PathSegment> segments, InputStream is) throws URISyntaxException, IOException {
        return artifactStorage.put(URIBuilder.from(segments).addPathFromSource().build(), is).toJson();
    }
}
