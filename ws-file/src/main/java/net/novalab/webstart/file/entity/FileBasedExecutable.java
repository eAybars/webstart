package net.novalab.webstart.file.entity;

import net.novalab.webstart.service.application.entity.AbstractExecutable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by ertunc on 01/06/17.
 */
public class FileBasedExecutable extends AbstractExecutable {
    private File baseDirectory;
    private File executable;

    public FileBasedExecutable(URI id, File base, File executable) {
        super(id);
        this.baseDirectory = base;
        this.executable = executable;
    }

    @Override
    public URL getExecutable() {
        try {
            return executable.toURI().toURL();
        } catch (MalformedURLException e) {
            //not expected
            throw new RuntimeException(e);
        }
    }

    @Override
    public URL getResource(String path) {
        if (path.matches(".*\\.\\./.*")) {//if contains ../ skip it for security reasons
            return null;
        }
        File file = new File(baseDirectory, path);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        try {
            return new File(baseDirectory, path).toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
