package com.eaybars.webstart.service.uri.control;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Set;
import java.util.stream.Stream;

/**
 * To add a URL connection handler with CDI capabilities, create a class in the form of
 * {@code net.novalab.webstart.service.uri.control.<protocol-name>.Handler extends CDIDelegatingURLStreamHandler} which has
 * an inner class int the form of {@code public static class <Class-Name> extends CDIDelegatingURLStreamHandler.CDIDelegate}.
 * This inner class should provide connection handling and can have CDI capabilities
 */
public class CDIDelegatingURLStreamHandler extends URLStreamHandler {

    private CDIDelegate delegate;
    private int matchingBeanSize;

    public CDIDelegatingURLStreamHandler() {
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beans = beanManager.getBeans(getDelegationType(), getDelegationTypeAnnotations());
        if ((matchingBeanSize = beans.size()) == 1) {
            Bean<CDIDelegate> bean = (Bean<CDIDelegate>) beans.iterator().next();
            CreationalContext<CDIDelegate> context = beanManager.createCreationalContext(bean);
            delegate = (CDIDelegate) beanManager.getReference(bean , getDelegationType(), context);
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        if (delegate == null) {
            throw new IOException(matchingBeanSize + " instances of matching delegation types found instead of 1");
        } else {
            return delegate.openConnection(u);
        }
    }


    protected Class<? extends CDIDelegate> getDelegationType() {
        return Stream.of(getClass().getClasses())
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .filter(CDIDelegate.class::isAssignableFrom)
                .map(c -> (Class<CDIDelegate>)c)
                .findFirst()
                .orElse(CDIDelegate.class);
    }

    protected Annotation[] getDelegationTypeAnnotations() {
        return new Annotation[0];
    }

    public static abstract class CDIDelegate extends URLStreamHandler {
        @Override
        protected abstract URLConnection openConnection(URL u) throws IOException;
    }
}
