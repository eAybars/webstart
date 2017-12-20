package com.eaybars.webstart.service.backend.control;

import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

public interface Backend {

    URI ROOT = URI.create("/");

    URI getName();

    Stream<URI> contents(URI parent);

    URL getResource(URI uri);

    default boolean isDirectory(URI uri) {
        String s = uri.toString();
        return "".equals(s) || s.endsWith("/");
    }

    Optional<Storage> getStorage();
}
