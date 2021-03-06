package com.eaybars.webstart.service.uri.control;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URIBuilderTest {
    @Test
    public void addPathFromSource() throws Exception {
        Assert.assertEquals("www.novalab.com.tr/apps/download", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addPathFromSource().build().toString());
        assertEquals("http://www.novalab.com.tr/apps/download", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addPathFromSource().build().toString());
        assertEquals("http://user@www.novalab.com.tr/apps/download", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addPathFromSource().build().toString());
        assertEquals("/apps/download", URIBuilder.from("/apps/download?compress=true")
                .addPathFromSource().build().toString());
        assertEquals("apps/download", URIBuilder.from("apps/download?compress=true")
                .addPathFromSource().build().toString());

        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .addPathFromSource().build().toString());
        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .addPathFromSource().build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .build().toString());
    }

    @Test
    public void addParentPathFromSource() throws Exception {
        assertEquals("www.novalab.com.tr/apps", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addParentPathFromSource().build().toString());
        assertEquals("http://www.novalab.com.tr/apps", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addParentPathFromSource().build().toString());
        assertEquals("http://user@www.novalab.com.tr/apps", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addParentPathFromSource().build().toString());
        assertEquals("/apps", URIBuilder.from("/apps/download?compress=true")
                .addParentPathFromSource().build().toString());
        assertEquals("apps", URIBuilder.from("apps/download?compress=true")
                .addParentPathFromSource().build().toString());
        assertEquals("apps", URIBuilder.from("apps/download")
                .addParentPathFromSource().build().toString());
        assertEquals("apps", URIBuilder.from("apps/download/")
                .addParentPathFromSource().build().toString());
        assertEquals("", URIBuilder.from("apps/")
                .addParentPathFromSource().build().toString());
        assertEquals("", URIBuilder.from("apps")
                .addParentPathFromSource().build().toString());
        assertEquals("", URIBuilder.from("/")
                .addParentPathFromSource().build().toString());

        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .addParentPathFromSource().build().toString());
        assertEquals("mailto:info@novalab.com.tr", URIBuilder.from("mailto:info@novalab.com.tr")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .addParentPathFromSource().build().toString());
        assertEquals("urn:isbn:096139210x/p1", URIBuilder.from("urn:isbn:096139210x/p1")
                .build().toString());
    }

    @Test
    public void addQueryFromSource() throws Exception {
        assertEquals("?compress=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource().build().toString());
        assertEquals("http://www.novalab.com.tr?compress=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource().build().toString());
        assertEquals("http://user@www.novalab.com.tr?compress=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource().build().toString());
        assertEquals("?compress=true", URIBuilder.from("/apps/download?compress=true")
                .addQueryFromSource().build().toString());
        assertEquals("?compress=true", URIBuilder.from("apps/download?compress=true")
                .addQueryFromSource().build().toString());

        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .addQueryFromSource().build().toString());
        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .addQueryFromSource().build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true", URIBuilder.from("urn:isbn:096139210x/p1?detail=true")
                .build().toString());
    }

    @Test
    public void addQuery() throws Exception {
        assertEquals("?test=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource()
                .addQuery("test=true").build().toString());

        assertEquals("http://www.novalab.com.tr?test=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("http://www.novalab.com.tr?compress=true&test=true", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource()
                .addQuery("test=true").build().toString());

        assertEquals("http://user@www.novalab.com.tr?test=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("http://user@www.novalab.com.tr?compress=true&test=true", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addQueryFromSource()
                .addQuery("test=true").build().toString());

        assertEquals("?test=true", URIBuilder.from("/apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("/apps/download?compress=true")
                .addQueryFromSource()
                .addQuery("test=true").build().toString());

        assertEquals("?test=true", URIBuilder.from("apps/download?compress=true")
                .addQuery("test=true").build().toString());
        assertEquals("?compress=true&test=true", URIBuilder.from("apps/download?compress=true")
                .addQueryFromSource()
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
    public void addFragmentFromSource() throws Exception {
        assertEquals("#p5", URIBuilder.from("www.novalab.com.tr/apps/download?compress=true#p5")
                .addFragmentFromSource().build().toString());
        assertEquals("http://www.novalab.com.tr#p5", URIBuilder.from("http://www.novalab.com.tr/apps/download?compress=true#p5")
                .addFragmentFromSource().build().toString());
        assertEquals("http://user@www.novalab.com.tr#p5", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true#p5")
                .addFragmentFromSource().build().toString());
        assertEquals("#p5", URIBuilder.from("/apps/download?compress=true#p5")
                .addFragmentFromSource().build().toString());
        assertEquals("#p5", URIBuilder.from("apps/download?compress=true#p5")
                .addFragmentFromSource().build().toString());

        assertEquals("mailto:info@novalab.com.tr?detail=true#p5", URIBuilder.from("mailto:info@novalab.com.tr?detail=true#p5")
                .addFragmentFromSource().build().toString());
        assertEquals("mailto:info@novalab.com.tr?detail=true", URIBuilder.from("mailto:info@novalab.com.tr?detail=true#p5")
                .build().toString());
        assertEquals("urn:isbn:096139210x/p1?detail=true#p5", URIBuilder.from("urn:isbn:096139210x/p1?detail=true#p5")
                .addFragmentFromSource().build().toString());
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
                .addPathFromSource()
                .addPath("some/path").build().toString());
        assertEquals("http://user@www.novalab.com.tr/some/path", URIBuilder.from("http://user@www.novalab.com.tr/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("some/path", URIBuilder.from("/apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("/apps/download/some/path", URIBuilder.from("/apps/download?compress=true")
                .addPathFromSource()
                .addPath("some/path").build().toString());
        assertEquals("some/path", URIBuilder.from("apps/download?compress=true")
                .addPath("some/path").build().toString());
        assertEquals("apps/download/some/path", URIBuilder.from("apps/download?compress=true")
                .addPathFromSource()
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