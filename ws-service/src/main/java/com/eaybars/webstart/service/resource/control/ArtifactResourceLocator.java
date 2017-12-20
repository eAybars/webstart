package com.eaybars.webstart.service.resource.control;

import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.artifact.entity.Artifact;
import com.eaybars.webstart.service.backend.control.Backend;
import com.eaybars.webstart.service.filter.entity.AccessFilter;
import com.eaybars.webstart.service.filter.entity.AggregatedFilter;
import jnlp.sample.resource.ResourceLocator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.function.Predicate;

@ApplicationScoped
public class ArtifactResourceLocator implements ResourceLocator {

    @Inject
    Backend backend;
    @Inject
    Artifacts artifacts;
    @Inject
    @AggregatedFilter
    @AccessFilter
    Predicate<Artifact> accessFilter;

    @Override
    public URL apply(String path) {
        URI pathUri = URI.create(path);
        return artifacts.hierarchy().parents(pathUri)
                .max(Comparator.naturalOrder())
                .map(a -> accessFilter.test(a) ? backend.getResource(pathUri) : null)
                .orElse(null);
    }

}