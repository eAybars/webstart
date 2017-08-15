package jnlp.sample.servlet;

import java.net.URL;
import java.util.function.Function;

public interface ResourceLocator extends Function<String, URL> {
}
