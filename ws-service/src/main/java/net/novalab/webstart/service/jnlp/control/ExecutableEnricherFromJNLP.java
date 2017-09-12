package net.novalab.webstart.service.jnlp.control;

import net.novalab.webstart.service.artifact.control.ArtifactEvent;
import net.novalab.webstart.service.artifact.entity.AbstractArtifact;
import net.novalab.webstart.service.artifact.entity.Executable;
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

    public void onExecutableLoaded(@Observes(notifyObserver = Reception.ALWAYS, during = TransactionPhase.IN_PROGRESS)
                                   @ArtifactEvent(ArtifactEvent.Type.LOADED)
                                           Executable executable) {
        try {
            URL url = executable.getResource(executable.getExecutable().toString());
            if (url != null) {
                JNLPInfo jnlpInfo = JNLPInfo.from(url);
                if (executable instanceof AbstractArtifact) {
                    AbstractArtifact abstractArtifact = (AbstractArtifact) executable;
                    jnlpInfo.getTitle().ifPresent(abstractArtifact::setTitle);
                    jnlpInfo.getDescription().ifPresent(abstractArtifact::setDescription);
                    jnlpInfo.getIcon()
                            .map(executable.getIdentifier()::relativize)
                            .ifPresent(abstractArtifact::setIcon);
                }
                try {
                    Method setVersion = executable.getClass().getMethod("setVersion", String.class);
                    setVersion.invoke(executable, jnlpInfo.getVersion());
                } catch (NoSuchMethodException | IllegalAccessException e) {
                } catch (InvocationTargetException e) {
                    LOGGER.log(Level.WARNING, "An error occurred while setting executable version", e);
                }
                jnlpInfo.getVendor().ifPresent(v -> executable.getAttributes().put("vendor", v));
            }
        } catch (IOException | SAXException e) {
            LOGGER.log(Level.WARNING, "An error occurred while parsing the jnlp file", e);
        }
    }
}
