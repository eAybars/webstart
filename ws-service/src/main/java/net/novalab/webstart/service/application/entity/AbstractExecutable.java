package net.novalab.webstart.service.application.entity;

import java.net.URI;
import java.util.Date;
import java.util.Properties;

/**
 * Created by ertunc on 29/05/17.
 */
public abstract class AbstractExecutable extends SimpleComponent implements Executable {
    private String version;
    private Date dateModified;
    private Properties attributes;

    public AbstractExecutable(URI identifier) {
        super(identifier);
        this.dateModified = new Date();
        this.attributes = new Properties();
    }

    @Override
    public Properties getAttributes() {
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

}
