package jnlp.sample.util;

import jnlp.sample.servlet.ResourceLocator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Enumeration;
import java.util.function.Function;

/**
 * Created by ertunc on 25/08/15.
 */
public class DelegateServletConfig implements ServletConfig {

    private ServletConfig delegate;
    private ServletContext context;

    public DelegateServletConfig(ResourceLocator resourceLocator, ServletConfig delegate) {
        this.delegate = delegate;
        this.context = new DelegateContext(resourceLocator, delegate.getServletContext());
    }

    @Override
    public String getServletName() {
        return delegate.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return delegate.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();
    }
}
