package net.novalab.webstart.service.application.entity;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ertunc on 29/05/17.
 */
public interface Executable extends Component {

    Map<String, Object> getAttributes();

    String getVersion();

    Date getDateModified();

    URI getExecutable();
}
