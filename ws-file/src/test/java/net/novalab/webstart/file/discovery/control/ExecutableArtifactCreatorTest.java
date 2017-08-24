package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedExecutable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ExecutableArtifactCreatorTest {

    @Rule
    public TemporaryFolder artifactRootRule = new TemporaryFolder();

    private ExecutableArtifactCreator executableArtifactCreator;
    private File appFolder, pdfFolder;


    @Before
    public void setUp() throws Exception {
        appFolder = artifactRootRule.newFolder("app");
        pdfFolder = artifactRootRule.newFolder("pdf");

        if (!new File(appFolder, "app.jnlp").createNewFile() ||
                !new File(appFolder, "build.jnlp").createNewFile() ||
                !new File(pdfFolder, "tutorial.pdf").createNewFile()){
            throw new RuntimeException("Cannot create files");
        }

        executableArtifactCreator = new ExecutableArtifactCreator();
        executableArtifactCreator.artifactRoot = artifactRootRule.getRoot();
    }

    @Test
    public void apply() throws Exception {
        assertEquals(0, executableArtifactCreator.apply(artifactRootRule.getRoot()).count());
        assertEquals(0, executableArtifactCreator.apply(pdfFolder).count());

        Set<? extends FileBasedArtifact> artifacts = executableArtifactCreator.apply(appFolder).collect(Collectors.toSet());
        assertEquals(1, artifacts.size());

        FileBasedArtifact artifact = artifacts.iterator().next();
        assertTrue(artifact instanceof FileBasedExecutable);

        FileBasedExecutable executable = (FileBasedExecutable) artifact;
        assertEquals(new URI("app.jnlp"), executable.getExecutable());
        assertEquals(appFolder, executable.getIdentifierFile());
    }

}