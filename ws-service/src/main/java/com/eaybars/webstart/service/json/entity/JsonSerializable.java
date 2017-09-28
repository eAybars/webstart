package com.eaybars.webstart.service.json.entity;

import javax.json.JsonObject;

@FunctionalInterface
public interface JsonSerializable {

    JsonObject toJson();
}
