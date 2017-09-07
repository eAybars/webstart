package net.novalab.webstart.service.artifact.control;

import net.novalab.webstart.service.uri.control.URIBuilder;
import org.junit.Test;

import static org.junit.Assert.*;

public class URIBuilderTest {
    @Test
    public void addOriginalPath() throws Exception {
        assertEquals("www.novalab.com.tr/apps/download", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addOriginalPath().build().toString());
        assertEquals("http://www.novalab.com.tr/apps/download", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addOriginalPath().build().toString());
        assertEquals("http://user@www.novalab.com.tr/apps/download", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addOriginalPath().build().toString());
        assertEquals("/apps/download", URIBuilder.from("/apps/download?compress=true")
                .addOriginalPath().build().toString());
        assertEquals("apps/download", URIBuilder.from("apps/download?compress=true")
                .addOriginalPath().build().toString());

        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .addOriginalPath().build().toString());
        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .addOriginalPath().build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .build().toString());
    }

    @Test
    public void addParentOfOriginalPath() throws Exception {
        assertEquals("www.novalab.com.tr/apps", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addParentOfOriginalPath().build().toString());
        assertEquals("http://www.novalab.com.tr/apps", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addParentOfOriginalPath().build().toString());
        assertEquals("http://user@www.novalab.com.tr/apps", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addParentOfOriginalPath().build().toString());
        assertEquals("/apps", URIBuilder.from("/apps/download?compress=true")
                .addParentOfOriginalPath().build().toString());
        assertEquals("apps", URIBuilder.from("apps/download?compress=true")
                .addParentOfOriginalPath().build().toString());

        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .addParentOfOriginalPath().build().toString());
        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .addParentOfOriginalPath().build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .build().toString());
    }

    @Test
    public void addOriginalQuery() throws Exception {
        assertEquals("?compress=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery().build().toString());
        assertEquals("http://www.novalab.com.tr?compress=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery().build().toString());
        assertEquals("http://user@www.novalab.com.tr?compress=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery().build().toString());
        assertEquals("?compress=true", URIBuilder.from("/apps/download?compress=true")
                .addOriginalQuery().build().toString());
        assertEquals("?compress=true", URIBuilder.from("apps/download?compress=true")
                .addOriginalQuery().build().toString());

        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .addOriginalQuery().build().toString());
        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .addOriginalQuery().build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .build().toString());
    }

    @Test
    public void addQuery() throws Exception {
        assertEquals("?test=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery()
                .addQuery("test=true").build().toString());

        assertEquals("http://www.novalab.com.tr?test=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("http://www.novalab.com.tr?compress=true&test=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery()
                .addQuery("test=true").build().toString());

        assertEquals("http://user@www.novalab.com.tr?test=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("http://user@www.novalab.com.tr?compress=true&test=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addOriginalQuery()
                .addQuery("test=true").build().toString());

        assertEquals("?test=true", URIBuilder.from("/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("/apps/download?compress=true")
                .addOriginalQuery()
                .addQuery("test=true").build().toString());

        assertEquals("?test=true", URIBuilder.from("apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("apps/download?compress=true")
                .addOriginalQuery()
                .addQuery("test=true").build().toString());

        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .addQuery("test=true").build().toString());
        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .addQuery("test=true").build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .build().toString());
    }

    @Test
    public void addOriginalFragment() throws Exception {
        assertEquals("#p5", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("http://www.novalab.com.tr#p5", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("http://user@www.novalab.com.tr#p5", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("#p5", URIBuilder.from("/apps/download?compress=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("#p5", URIBuilder.from("apps/download?compress=true#p5")
                .addOriginalFragment().build().toString());

        assertEquals("mailto:info@novalab.com.tr?detail=true#p5", URIBuilder.from("mailto:info@novalab.com.tr?detail=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true#p5")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true#p5", URIBuilder.from("urn:isbn:096139210x/p1?detail=true#p5")
                .addOriginalFragment().build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true#p5")
                .build().toString());
    }

    @Test
    public void addPath() throws Exception {
        assertEquals("some/path", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("http://www.novalab.com.tr/some/path", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("http://www.novalab.com.tr/apps/download/some/path", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addOriginalPath()
                .addPath("some/path").build().toString());
        assertEquals("http://user@www.novalab.com.tr/some/path", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("some/path", URIBuilder.from("/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("/apps/download/some/path", URIBuilder.from("/apps/download?compress=true")
                .addOriginalPath()
                .addPath("some/path").build().toString());
        assertEquals("some/path", URIBuilder.from("apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("apps/download/some/path", URIBuilder.from("apps/download?compress=true")
                .addOriginalPath()
                .addPath("some/path").build().toString());

        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .addPath("some/path").build().toString());
        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .addPath("some/path").build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .build().toString());
    }

}