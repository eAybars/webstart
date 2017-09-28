package com.eaybars.webstart.service.json.control;

import javax.json.*;

public class Enrich {

    public static JsonObjectBuilder object(JsonObject object) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        object.forEach(builder::add);
        return builder;
    }

    public static JsonArrayBuilder array(JsonArray array) {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        array.forEach(builder::add);
        return builder;
    }
}
