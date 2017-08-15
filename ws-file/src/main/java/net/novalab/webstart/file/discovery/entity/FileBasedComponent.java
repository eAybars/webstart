package net.novalab.webstart.file.discovery.entity;

import net.novalab.webstart.service.application.entity.AbstractComponent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by ertunc on 20/06/17.
 */
public class FileBasedComponent extends AbstractComponent {
    private File baseDirectory;

    public FileBasedComponent(URI identifier, File baseDirectory) {
        super(identifier);
        this.baseDirectory = Objects.requireNonNull(baseDirectory);
        this.setTitle(baseDirectory.getName());
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    @Override
    public URL getResource(String path) {
        if (path == null || "".equals(path) ||
                path.matches(".*\\.\\./.*")) {//if contains ../ skip it for security reasons
            return null;
        }
        File file = new File(getBaseDirectory(), path);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        try {
            return new File(getBaseDirectory(), path).toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FileBasedComponent component = (FileBasedComponent) o;

        return getBaseDirectory().equals(component.getBaseDirectory());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getBaseDirectory().hashCode();
        return result;
    }
}
