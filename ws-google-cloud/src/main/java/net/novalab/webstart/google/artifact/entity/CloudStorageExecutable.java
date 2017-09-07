package net.novalab.webstart.google.artifact.entity;

import net.novalab.webstart.service.artifact.entity.Executable;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CloudStorageExecutable extends CloudStorageComponent implements Executable{

    private URI executable;
    private Map<String, Object> attributes;
    private String version;
    private Date dateModified;

    public CloudStorageExecutable(URI identifier, URI executable) {
        super(identifier);
        this.executable = executable;
        this.attributes = new HashMap<>();
        this.version = "";
    }

    @Override
    public Map<String, Object> getAttributes() {
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
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Override
    public URI getExecutable() {
        return executable;
    }
}
