package net.novalab.webstart.service.filter.control;

import net.novalab.webstart.service.artifact.entity.Artifact;
import net.novalab.webstart.service.backend.control.Backend;
import net.novalab.webstart.service.backend.control.Backends;
import net.novalab.webstart.service.discovery.control.ResourcesArtifactDiscovery;
import net.novalab.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.util.function.Predicate;

@VisibilityFilter
@ApplicationScoped
public class ResourcesFilter implements Predicate<Artifact> {
    @Inject
    Backends backends;

    @Override
    public boolean test(Artifact artifact) {
        return !ResourcesArtifactDiscovery.RESOURCES_URI.equals(artifact.getIdentifier()) &&
                backends.stream()
                        .map(Backend::getName)
                        .map(uri -> URI.create(uri.toString() + ResourcesArtifactDiscovery.RESOURCES_URI))
                        .noneMatch(artifact.getIdentifier()::equals);
    }
}
