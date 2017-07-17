package net.novalab.webstart.service.application.entity;

import java.util.Optional;

/**
 * Created by ertunc on 17/07/17.
 */
public interface ResourceRoot {
    public static final String PATH = Optional.ofNullable(System.getenv("WEB_START_RESOURCE_PATH"))
            .orElse(System.getProperty("WEB_START_RESOURCE_PATH", "download"));
}
