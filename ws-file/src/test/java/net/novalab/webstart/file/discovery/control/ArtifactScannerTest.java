package net.novalab.webstart.file.discovery.control;

import net.novalab.webstart.file.artifact.entity.FileBasedArtifact;
import net.novalab.webstart.file.artifact.entity.FileBasedComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.enterprise.inject.Instance;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArtifactScannerTest {

    @Rule
    public TemporaryFolder artifactRootRule = new TemporaryFolder();

    private ArtifactScanner artifactScanner;
    private ArtifactCreator ac1, ac2;
    private FileBasedArtifact artifact1, artifact2;
    private File path1,path1Sub1, path1SubSub1;
    private File path2, path2Sub1;

    @Before
    public void setUp() throws Exception {
        path1SubSub1 = artifactRootRule.newFolder("path1", "path1-sub1", "path1-sub-sub1");
        path1Sub1 = path1SubSub1.getParentFile();
        path1 = path1Sub1.getParentFile();

        path2Sub1 = artifactRootRule.newFolder("path2", "path2-sub1");
        path2 = path2Sub1.getParentFile();

        artifactScanner = new ArtifactScanner();

        artifact1 = mock(FileBasedArtifact.class);
        when(artifact1.toString()).thenReturn("Artifact - 1");
        artifact2 = mock(FileBasedArtifact.class);
        when(artifact2.toString()).thenReturn("Artifact - 2");

        ac1 = mock(ArtifactCreator.class);
        when(ac1.apply(any())).thenAnswer(a -> path1SubSub1.equals(a.getArguments()[0]) ? Stream.of(artifact1) : Stream.empty());
        ac2 = mock(ArtifactCreator.class);
        when(ac2.apply(any())).thenAnswer(a -> path2.equals(a.getArguments()[0]) ? Stream.of(artifact2) : Stream.empty());

        artifactScanner.artifactCreators = mock(Instance.class);
        artifactScanner.artifactRoot = artifactRootRule.getRoot();
        when(artifactScanner.artifactCreators.spliterator()).thenAnswer(a -> Arrays.asList(ac1, ac2, artifactScanner).spliterator());
    }

    @Test
    public void apply() throws Exception {
        Set<? extends FileBasedArtifact> artifacts = artifactScanner.apply(artifactRootRule.getRoot()).collect(Collectors.toSet());

        verify(ac1).apply(artifactRootRule.getRoot());
        verify(ac1).apply(path1);
        verify(ac1).apply(path1Sub1);
        verify(ac1).apply(path1SubSub1);
        verify(ac1).apply(path2);
        verify(ac1).apply(path2Sub1);

        verify(ac2).apply(artifactRootRule.getRoot());
        verify(ac2).apply(path1);
        verify(ac2).apply(path1Sub1);
        verify(ac2).apply(path1SubSub1);
        verify(ac2).apply(path2);
        verify(ac2).apply(path2Sub1);

        assertEquals(5, artifacts.size());
        assertTrue(artifacts.contains(artifact1));
        assertTrue(artifacts.contains(artifact2));
        assertTrue(artifacts.contains(new FileBasedComponent(artifactScanner.toIdentifier(path1Sub1), path1Sub1)));
        assertTrue(artifacts.contains(new FileBasedComponent(artifactScanner.toIdentifier(path1), path1)));
        assertTrue(artifacts.contains(new FileBasedComponent(artifactScanner.toIdentifier(artifactRootRule.getRoot()), artifactRootRule.getRoot())));
    }

}