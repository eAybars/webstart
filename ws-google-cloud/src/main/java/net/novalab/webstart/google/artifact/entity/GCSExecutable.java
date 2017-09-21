package net.novalab.webstart.google.artifact.entity;

import net.novalab.webstart.service.artifact.entity.Executable;
import net.novalab.webstart.service.uri.control.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GCSExecutable extends GCSComponent implements Executable{

    private URI executable;
    private Map<String, Object> attributes;
    private String version;
    private Date dateModified;

    public GCSExecutable(URI executable) throws URISyntaxException {
        super(URIBuilder.from(executable).addParentPathFromSource().addPath("/").build());
        this.executable = getIdentifier().relativize(GCSArtifact.toIdentifierURI(executable));
        this.attributes = new HashMap<>();
        this.dateModified = new Date();
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
