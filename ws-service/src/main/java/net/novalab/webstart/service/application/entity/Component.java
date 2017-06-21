package net.novalab.webstart.service.application.entity;

import java.io.Serializable;
import java.net.URI;
import java.net.URL;

/**
 * Created by ertunc on 30/05/17.
 */
public interface Component extends Serializable, Comparable<Component> {

    URI getIdentifier();

    String getTitle();

    String getDescription();

    URI getIcon();

    URL getResource(String path);

    @Override
    default int compareTo(Component o){
        return getIdentifier().compareTo(o.getIdentifier());
    }
}
