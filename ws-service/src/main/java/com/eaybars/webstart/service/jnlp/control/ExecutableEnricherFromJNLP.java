package com.eaybars.webstart.service.jnlp.control;

import com.eaybars.webstart.service.artifact.entity.ArtifactEvent;
import com.eaybars.webstart.service.artifact.entity.Executable;
import com.eaybars.webstart.service.backend.control.Backend;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ExecutableEnricherFromJNLP {
    private static final Logger LOGGER = Logger.getLogger(ExecutableEnricherFromJNLP.class.getName());

    @Inject
    Backend backend;

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
            URL url = backend.getResource(executable.getIdentifier().resolve(executable.getExecutable()));
            if (url != null) {
                JNLPInfo jnlpInfo = JNLPInfo.from(url);
                jnlpInfo.getTitle().ifPresent(executable::setTitle);
                if (executable.getIcon() == null) {
                    jnlpInfo.getIcon()
                            .map(executable.getIdentifier()::relativize)
                            .ifPresent(executable::setIcon);
                }
                if (executable.getVersion() == null) {
                    executable.setVersion(jnlpInfo.getVersion());
                }

                JsonObjectBuilder attributes = executable.attributes();
                jnlpInfo.getDescription().ifPresent(d -> attributes.add("description", d));

                jnlpInfo.getVendor().ifPresent(v -> attributes.add("vendor", v));
                attributes.build();
            }
        } catch (IOException | SAXException e) {
            LOGGER.log(Level.WARNING, "An error occurred while parsing the jnlp file", e);
        }
    }
}
