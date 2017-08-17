package net.novalab.webstart.file.component.entity;

import net.novalab.webstart.service.component.entity.Executable;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ertunc on 01/06/17.
 */
public class FileBasedExecutable extends FileBasedComponent implements Executable {
    private File executable;
    private Map<String, Object> attributes;
    private String version;

    public FileBasedExecutable(URI id, File base, File executable) {
        super(id, base);
        this.executable = executable;
        this.attributes = new TreeMap<>();
        this.version = "";
    }

    @Override
    public Map<String, Object>  getAttributes() {
        return attributes;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public Date getDateModified() {
        return new Date(getBaseDirectory().lastModified());
    }

    @Override
    public URI getExecutable() {
        return getBaseDirectory().toURI().relativize(executable.toURI());
    }

    public File getExecutableFile() {
        return executable;
    }
}
