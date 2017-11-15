package com.eaybars.webstart.service.filter.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.backend.control.Backends;
import com.eaybars.webstart.service.discovery.control.ResourcesArtifactDiscovery;
import com.eaybars.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.function.Predicate;

@VisibilityFilter
@ApplicationScoped
public class ResourcesFilter implements Predicate<Artifact> {
    public static final URI RESOURCES_URI =URI.create("/" + ResourcesArtifactDiscovery.RESOURCES_URI);
    @Inject
    Backends backends;

    @Override
    public boolean test(Artifact artifact) {
        return !RESOURCES_URI.equals(artifact.getIdentifier()) &&
                backends.stream()
                        .map(Backend::getName)
                        .map(uri -> URI.create(uri.toString() + ResourcesArtifactDiscovery.RESOURCES_URI))
                        .noneMatch(artifact.getIdentifier()::equals);
    }
}
