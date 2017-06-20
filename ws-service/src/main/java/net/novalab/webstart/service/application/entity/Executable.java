package net.novalab.webstart.service.application.entity;

import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.function.Function;

/**
 * Created by ertunc on 29/05/17.
 */
public interface Executable extends Component {

    Properties getAttributes();

    String getVersion();

    Date getDateModified();

    URL getExecutable();

    URL getResource(String path);
}
