package net.novalab.webstart.service.artifact.entity;

/**
 * Represents a downloadable resource like a document. Their identifier URI points to the downloadable item itself and
 * as a consequence, it should not end with a "/" character. Resource artifacts may not have sub components for that reason
 */
public interface Resource extends Artifact {

}
