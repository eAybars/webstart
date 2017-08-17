package net.novalab.webstart.file.component.entity;

import net.novalab.webstart.service.component.entity.AbstractComponent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

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

}
