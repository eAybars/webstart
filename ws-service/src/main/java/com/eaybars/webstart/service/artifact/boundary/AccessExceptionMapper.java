package com.eaybars.webstart.service.artifact.boundary;

import javax.ejb.EJBAccessException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AccessExceptionMapper implements ExceptionMapper<EJBAccessException> {
    @Override
    public Response toResponse(EJBAccessException exception) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
