package net.novalab.webstart.service.discovery.boundary;

import net.novalab.webstart.service.json.entity.JsonErrorResponse;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;

public class NotImplementedException extends ServerErrorException {

    public NotImplementedException() {
        super(Response.Status.NOT_IMPLEMENTED);
    }

    public NotImplementedException(String message) {
        super(message, Response.Status.NOT_IMPLEMENTED);
    }

    public NotImplementedException(JsonErrorResponse response) {
        super(Response.status(Response.Status.NOT_IMPLEMENTED).entity(response.toJson()).build());
    }

}
