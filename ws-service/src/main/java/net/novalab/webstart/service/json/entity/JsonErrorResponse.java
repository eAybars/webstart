package net.novalab.webstart.service.json.entity;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonErrorResponse implements JsonSerializable {

    private String message;
    private String details;

    public JsonErrorResponse(String message) {
        this.message = message;
    }

    public JsonErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public JsonObject toJson() {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("message", this.message);
        if (details != null) {
            builder.add("details", details);
        }
        return builder.build();
    }
}
