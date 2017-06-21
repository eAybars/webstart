package net.novalab.webstart.file.discovery.entity;

import net.novalab.webstart.service.application.entity.Executable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Created by ertunc on 01/06/17.
 */
public class FileBasedExecutable extends FileBasedComponent implements Executable {
    private File executable;
    private Properties properties;
    private String version;

    public FileBasedExecutable(URI id, File base, File executable) {
        super(id, base);
        this.executable = executable;
        this.properties = new Properties();
        this.version = "";
    }

    @Override
    public Properties getAttributes() {
        return properties;
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


}
