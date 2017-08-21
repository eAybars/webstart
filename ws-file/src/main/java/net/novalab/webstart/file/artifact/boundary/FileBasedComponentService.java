package net.novalab.webstart.file.artifact.boundary;

import net.novalab.webstart.file.artifact.control.FileBasedArtifactSupplier;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("component/file")
@Consumes("text/plain")
@RolesAllowed("Admin")
@Stateless
public class FileBasedComponentService {
    @Inject
    FileBasedArtifactSupplier componentSupplier;

    @POST
    @Path("reload")
    public Response reload(String path) {
        if (path == null) {
            componentSupplier.reloadAll();
        } else {
            File file = new File(componentSupplier.root().toFile(), path);
            if (file.exists() && file.isDirectory()) {
                componentSupplier.reloadComponents(file.toPath());
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("update")
    public Response update(String path) {
        if (path == null) {
            componentSupplier.updateComponents(componentSupplier.root());
        } else {
            File file = new File(componentSupplier.root().toFile(), path);
            if (file.exists() && file.isDirectory()) {
                componentSupplier.updateComponents(file.toPath());
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("unload")
    public Response unload(String path) {
        if (path == null) {
            componentSupplier.unloadComponents(componentSupplier.root());
        } else {
            File file = new File(componentSupplier.root().toFile(), path);
            if (file.exists() && file.isDirectory()) {
                componentSupplier.unloadComponents(file.toPath());
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        return Response.ok().build();
    }


}
