package net.novalab.webstart.file.control;

import net.novalab.webstart.file.entity.FileBasedExecutable;
import net.novalab.webstart.service.application.entity.Component;
import net.novalab.webstart.service.application.entity.Executable;
import net.novalab.webstart.service.application.entity.SimpleComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by ertunc on 20/06/17.
 */
@ApplicationScoped
public class ComponentScanner implements Function<File, List<Component>> {

    @Inject
    @ArtifactRoot
    private File artifactRoot;

    @Override
    public List<Component> apply(File folder) {
        List<Component> components = new LinkedList<>();
        Component c = Stream.of(folder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".jnlp")))
                .sorted(Comparator.comparing(File::getName))
                .findFirst()
                .<Component>map(exe -> new FileBasedExecutable(artifactRoot.toURI().relativize(folder.toURI()), folder, exe))
                .orElseGet(() -> {
                    SimpleComponent component = new SimpleComponent(artifactRoot.toURI().relativize(folder.toURI()));
                    return component;
                });
        components.add(c);
        for (File subDirectory : folder.listFiles(File::isDirectory)) {
            List<Component> subComponents = apply(subDirectory);
            if (subComponents.stream().noneMatch(Executable.class::isInstance)){
                subComponents.clear();
            }
            components.addAll(subComponents);
        }
        if (components.stream().noneMatch(Executable.class::isInstance)){
            components.clear();
        }
        return components;
    }
}
