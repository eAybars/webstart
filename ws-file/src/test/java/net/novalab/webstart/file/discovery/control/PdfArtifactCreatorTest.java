package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedResource;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class PdfArtifactCreatorTest {
    @Rule
    public TemporaryFolder artifactRootRule = new TemporaryFolder();
    private File appFolder, pdfFolder;
    private PdfArtifactCreator pdfArtifactCreator;

    @Before
    public void setUp() throws Exception {
        appFolder = artifactRootRule.newFolder("app");
        pdfFolder = artifactRootRule.newFolder("pdf");

        if (!new File(appFolder, "app.jnlp").createNewFile() ||
                !new File(appFolder, "build.jnlp").createNewFile() ||
                !new File(pdfFolder, "tutorial.pdf").createNewFile() ||
                !new File(pdfFolder, "quickStart.pdf").createNewFile()){
            throw new RuntimeException("Cannot create files");
        }

        pdfArtifactCreator = new PdfArtifactCreator();
        pdfArtifactCreator.artifactRoot = artifactRootRule.getRoot();
    }

    @Test
    public void apply() throws Exception {
        assertEquals(0, pdfArtifactCreator.apply(appFolder).count());

        Set<? extends FileBasedArtifact> artifacts = pdfArtifactCreator.apply(pdfFolder).collect(Collectors.toCollection(TreeSet::new));
        assertTrue(artifacts.stream().allMatch(FileBasedResource.class::isInstance));

        Iterator<? extends FileBasedArtifact> iterator = artifacts.iterator();
        FileBasedResource artifact = (FileBasedResource) iterator.next();
        assertEquals(new URI("/pdf/quickStart.pdf"), artifact.getIdentifier());
        artifact = (FileBasedResource) iterator.next();
        assertEquals(new URI("/pdf/tutorial.pdf"), artifact.getIdentifier());
        assertFalse(iterator.hasNext());
    }

}