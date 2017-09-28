package com.eaybars.webstart.service.artifact.boundary;

import com.eaybars.webstart.service.json.entity.JsonErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.URISyntaxException;

@Provider
public class URISyntaxExceptionMapper implements ExceptionMapper<URISyntaxException> {

    @Override
    public Response toResponse(URISyntaxException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new JsonErrorResponse(exception.getLocalizedMessage()).toJson())
                .build();
    }

}
