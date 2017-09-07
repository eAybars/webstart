package net.novalab.webstart.service.uri.control;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@Startup
public class URLStreamHandlerRegistry {

    private AtomicBoolean registrationCompleted = new AtomicBoolean(false);

    @PostConstruct
    public void register() {
        if (!registrationCompleted.getAndSet(true)) {
            System.setProperty("java.protocol.handler.pkgs",
                    Optional.ofNullable(System.getProperty("java.protocol.handler.pkgs"))
                            .map(p -> p.concat("|"))
                            .orElse("")
                            .concat(getClass().getPackage().getName())
                            .concat(".protocol"));
        }
    }

}
