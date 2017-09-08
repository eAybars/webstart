package net.novalab.webstart.file.artifact.boundary;

import net.novalab.webstart.file.artifact.control.FileBasedArtifactSupplier;
import net.novalab.webstart.service.uri.control.URIBuilder;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.PathSegment;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

@Stateless
@Path("artifact/local")
@RolesAllowed("Admin")
public class FileBasedArtifactService {
    @Inject
    FileBasedArtifactSupplier artifactSupplier;

    @POST
    @Path("reload")
    public JsonObject reload() {
        return artifactSupplier.reloadAll().toJson();
    }

    @POST
    @Path("reload/{segments: .+}")
    public JsonObject reload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        String path = URIBuilder.from(segments).addPathFromSource().build().toString();
        File file = new File(artifactSupplier.root().toFile(), path);
        if (file.exists() && file.isDirectory()) {
            return artifactSupplier.reload(file.toPath()).toJson();
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    @Path("update/")
    public JsonObject update() {
        return artifactSupplier.update(artifactSupplier.root()).toJson();
    }

    @POST
    @Path("update/{segments: .+}")
    public JsonObject update(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        String path = URIBuilder.from(segments).addPathFromSource().build().toString();
        File file = new File(artifactSupplier.root().toFile(), path);
        if (file.exists() && file.isDirectory()) {
            return artifactSupplier.update(file.toPath()).toJson();
        } else {
            throw new NotFoundException();
        }
    }

    @POST
    @Path("unload")
    public JsonObject unload() {
        return artifactSupplier.unload(artifactSupplier.root()).toJson();
    }

    @POST
    @Path("unload/{segments: .+}")
    public JsonObject unload(@PathParam("segments") List<PathSegment> segments) throws URISyntaxException {
        String path = URIBuilder.from(segments).addPathFromSource().build().toString();
        File file = new File(artifactSupplier.root().toFile(), path);
        if (file.exists() && file.isDirectory()) {
            return artifactSupplier.unload(file.toPath()).toJson();
        } else {
            throw new NotFoundException();
        }
    }


}
