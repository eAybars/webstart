package net.novalab.webstart.service.artifact.entity;

import java.net.URI;
import java.util.Optional;

/**
 * Represents an artifact which may have sub components. Therefore, a component identifier URI must end with a "/" character
 */
public interface Component extends Artifact {

    /**
     * Resolves a given URI according to the followings:<br>
     * <li>If the URI starts with a trailing / character, it is considered to be relative to the root of artifacts. By default
     * [application-domain]/download (i.e. www.myapplication.com/download) is the root for all artifacts. So if the reference
     * URI is /icons/myIcon.png than it is interpreted as www.myapplication.com/download/icons/myIcon.png</li>
     * <li>If the URI does not starts with a trailing / character, it is considered to be relative to the identifier URI
     * of the artifact. If this artifact belongs to a domain /my-component/
     * and the reference URI is icons/myIcon.png than it is interpreted as [application-domain]/download/my-component/icons/myIcon.png</li>
     * @param resource
     * @return
     */
    @Override
    default URI resolve(URI resource) {
        if (resource.toString().charAt(0) == '/') {
            return resource;
        } else {
            return URI.create(getIdentifier().toString() + resource);
        }
    }

    @Override
    default Optional<String> toRelativePath(URI uri) {
        if (uri.toString().startsWith(getIdentifier().toString())) {
            return Optional.of(uri.toString().substring(getIdentifier().toString().length()));
        } else {
            return Optional.empty();
        }
    }
}
