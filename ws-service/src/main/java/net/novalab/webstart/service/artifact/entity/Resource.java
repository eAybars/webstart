package net.novalab.webstart.service.artifact.entity;

import java.net.URI;
import java.util.Optional;

/**
 * Represents a downloadable resource like a document and therefore may not have sub components. Resource identifier URI
 * identifies the downloadable item itself and as a consequence, it should not end with a "/" character.
 */
public interface Resource extends Artifact {

    /**
     * Resolves a given URI according to the followings:<br>
     * <li>If the URI starts with a trailing / character, it is considered to be relative to the root of artifacts. By default
     * [application-domain]/download (i.e. www.myapplication.com/download) is the root for all artifacts. So if the reference
     * URI is /icons/myIcon.png than it is interpreted as www.myapplication.com/download/icons/myIcon.png</li>
     * <li>If the URI does not starts with a trailing / character, it is considered to be relative to the immediate parent
     * fragment of identifier URI for this resource. If the identifier URI for this resource is /documents/tutorial.pdf
     * and the reference URI is icons/myIcon.png than it is interpreted as [application-domain]/download/documents/icons/myIcon.png</li>
     * @param resource
     * @return
     */
    @Override
    default URI resolve(URI resource) {
        if (resource.toString().charAt(0) == '/') {
            return resource;
        } else {
            String parent = getIdentifier().toString();
            parent = parent.substring(0, parent.lastIndexOf('/'));
            return URI.create(parent + "/" + resource);
        }
    }

    @Override
    default Optional<String> toRelativePath(URI uri) {
        if (uri.equals(getIdentifier())) {
            return Optional.of("");
        } else {
            String parent = getIdentifier().toString();
            parent = parent.substring(0, parent.lastIndexOf('/')) + "/";
            if (uri.toString().startsWith(parent)) {
                return Optional.of(uri.toString().substring(parent.length()));
            } else {
                return Optional.empty();
            }
        }
    }
}
