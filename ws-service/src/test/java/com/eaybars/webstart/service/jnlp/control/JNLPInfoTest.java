package com.eaybars.webstart.service.jnlp.control;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.*;

public class JNLPInfoTest {
    private JNLPInfo jnlpInfo;

    @Before
    public void setUp() throws Exception {
        jnlpInfo = JNLPInfo.from(getClass().getClassLoader().getResource("launch.jnlp"));
    }

    @Test
    public void getVersion() throws Exception {
        assertEquals("4.3.180", jnlpInfo.getVersion());
    }

    @Test
    public void getTitle() throws Exception {
        Optional<String> title = jnlpInfo.getTitle();
        assertTrue(title.isPresent());
        assertEquals("JNLP Test File", title.get());
    }

    @Test
    public void getVendor() throws Exception {
        Optional<String> vendor = jnlpInfo.getVendor();
        assertTrue(vendor.isPresent());
        assertEquals("Novalab Teknoloji", vendor.get());
    }

    @Test
    public void getHomepage() throws Exception {
        Optional<String> homepage = jnlpInfo.getHomepage();
        assertFalse(homepage.isPresent());
    }

    @Test
    public void getDescription() throws Exception {
        Optional<String> description = jnlpInfo.getDescription();
        assertTrue(description.isPresent());
        assertEquals("A Web start deployment platform", description.get());
    }

    @Test
    public void getIcon() throws Exception {
        Optional<URI> icon = jnlpInfo.getIcon();
        assertTrue(icon.isPresent());
        assertEquals(URI.create("img/novalab-logo.png"), icon.get());
    }

}