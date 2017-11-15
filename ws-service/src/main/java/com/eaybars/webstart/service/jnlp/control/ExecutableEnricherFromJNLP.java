package com.eaybars.webstart.service.jnlp.control;

import com.eaybars.webstart.service.artifact.control.ArtifactEvent;
import com.eaybars.webstart.service.artifact.entity.AbstractArtifact;
import com.eaybars.webstart.service.artifact.entity.Executable;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ExecutableEnricherFromJNLP {
    private static final Logger LOGGER = Logger.getLogger(ExecutableEnricherFromJNLP.class.getName());

    public void onExecutableUpdated(@Observes(notifyObserver = Reception.ALWAYS, during = TransactionPhase.IN_PROGRESS)
                                   @ArtifactEvent(ArtifactEvent.Type.UPDATE)
                                           Executable executable) {
        enrich(executable);
    }


    public void onExecutableLoaded(@Observes(notifyObserver = Reception.ALWAYS, during = TransactionPhase.IN_PROGRESS)
                                   @ArtifactEvent(ArtifactEvent.Type.LOAD)
                                           Executable executable) {
        enrich(executable);
    }

    private void enrich(Executable executable) {
        try {
            URL url = executable.getResource(executable.getExecutable().toString());
            if (url != null) {
                JNLPInfo jnlpInfo = JNLPInfo.from(url);
                if (executable instanceof AbstractArtifact) {
                    AbstractArtifact abstractArtifact = (AbstractArtifact) executable;
                    if (abstractArtifact.getDescription() == null) {
                        jnlpInfo.getTitle().ifPresent(abstractArtifact::setTitle);
                        jnlpInfo.getDescription().ifPresent(abstractArtifact::setDescription);
                    }
                    if (abstractArtifact.getIcon() == null) {
                        jnlpInfo.getIcon()
                                .map(executable.getIdentifier()::relativize)
                                .ifPresent(abstractArtifact::setIcon);
                    }
                }
                if (executable.getVersion() == null) {
                    try {
                        Method setVersion = executable.getClass().getMethod("setVersion", String.class);
                        setVersion.invoke(executable, jnlpInfo.getVersion());
                    } catch (NoSuchMethodException | IllegalAccessException e) {
                    } catch (InvocationTargetException e) {
                        LOGGER.log(Level.WARNING, "An error occurred while setting executable version", e);
                    }
                }
                jnlpInfo.getVendor().ifPresent(v -> executable.getAttributes().putIfAbsent("vendor", v));
            }
        } catch (IOException | SAXException e) {
            LOGGER.log(Level.WARNING, "An error occurred while parsing the jnlp file", e);
        }
    }
}
