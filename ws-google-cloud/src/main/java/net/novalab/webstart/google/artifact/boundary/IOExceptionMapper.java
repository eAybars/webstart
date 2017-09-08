package net.novalab.webstart.google.artifact.boundary;

import net.novalab.webstart.service.json.entity.JsonErrorResponse;

import javax.ejb.EJBAccessException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class IOExceptionMapper implements ExceptionMapper<IOException> {
    @Override
    public Response toResponse(IOException exception) {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(new JsonErrorResponse(exception.getLocalizedMessage()))
                .build();
    }
}
