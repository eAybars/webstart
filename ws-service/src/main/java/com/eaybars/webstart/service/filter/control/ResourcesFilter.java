package com.eaybars.webstart.service.filter.control;

import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.discovery.control.ResourcesArtifactCreator;
import com.eaybars.webstart.service.filter.entity.VisibilityFilter;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Predicate;

@VisibilityFilter
@ApplicationScoped
public class ResourcesFilter implements Predicate<Artifact> {

    @Override
    public boolean test(Artifact artifact) {
        return !ResourcesArtifactCreator.RESOURCES_URI.equals(artifact.getIdentifier());
    }
}
