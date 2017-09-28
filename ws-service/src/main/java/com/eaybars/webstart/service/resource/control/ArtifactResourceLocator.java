package com.eaybars.webstart.service.resource.control;

import com.eaybars.webstart.service.artifact.control.Artifacts;
import com.eaybars.webstart.service.filter.entity.AccessFilter;
import com.eaybars.webstart.service.filter.entity.AggregatedFilter;
import jnlp.sample.servlet.ResourceLocator;
import com.eaybars.webstart.service.artifact.entity.Artifact;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

@ApplicationScoped
public class ArtifactResourceLocator implements ResourceLocator {

    @Inject
    Artifacts artifacts;
    @Inject
    @AggregatedFilter
    @AccessFilter
    Predicate<Artifact> accessFilter;

    @Override
    public URL apply(String path) {
        return toArtifactResource(path)
                .map(ArtifactResource::getResource)
                .orElse(null);
    }

    public Optional<ArtifactResource> toArtifactResource(String path) {
        URI pathUri = URI.create(path);
        return artifacts.stream()
                .sorted(Comparator.reverseOrder())
                .map(c -> new AbstractMap.SimpleImmutableEntry<>(c, c.toRelativePath(pathUri)))
                .filter(e -> e.getValue().isPresent())
                .findFirst()
                .map(e -> accessFilter.test(e.getKey()) ? e : null)
                .map(e -> new ArtifactResource(e.getKey(), e.getValue().get()));
    }

    public static class ArtifactResource {
        private Artifact artifact;
        private String path;

        public ArtifactResource(Artifact artifact, String path) {
            this.artifact = artifact;
            this.path = path;
        }

        public Artifact getArtifact() {
            return artifact;
        }

        public String getPath() {
            return path;
        }

        public URL getResource() {
            return artifact.getResource(path);
        }
    }
}
