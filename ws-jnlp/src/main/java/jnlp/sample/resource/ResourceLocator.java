package jnlp.sample.resource;

import java.net.URL;
import java.util.function.Function;

public interface ResourceLocator extends Function<String, URL> {
    String PATH = "/download";
}
