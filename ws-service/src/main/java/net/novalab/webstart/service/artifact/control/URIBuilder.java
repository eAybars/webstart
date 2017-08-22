package net.novalab.webstart.service.artifact.control;

import java.net.URI;
import java.net.URISyntaxException;

public class URIBuilder {

    private URI uri;
    private StringBuilder path;
    private StringBuilder query;
    private StringBuilder fragment;

    public URIBuilder(URI uri) {
        this.uri = uri;
        this.path = new StringBuilder();
        this.query = new StringBuilder();
        this.fragment = new StringBuilder();
    }

    public URIBuilder addOriginalPath() {
        if (uri.getPath() != null) {
            addPath(uri.getPath());
        }
        return this;
    }

    public URIBuilder addParentOfOriginalPath() {
        if (uri.getPath() != null) {
            int index = uri.getPath().lastIndexOf('/');
            if (index >= 0) {
                addPath(uri.getPath().substring(0, index));
            }
        }
        return this;
    }

    public URIBuilder addOriginalQuery() {
        if (uri.getQuery() != null) {
            query.append(uri.getQuery());
        }
        return this;
    }

    public URIBuilder addQuery(String query) {
        if (!uri.isOpaque()) {
            if (this.query.length() > 0) {
                this.query.append("&");
            }
            this.query.append(query);
        }
        return this;
    }

    public URIBuilder addOriginalFragment() {
        if (uri.getFragment() != null) {
            fragment.append(uri.getFragment());
        }
        return this;
    }

    public URIBuilder addPath(String path) {
        if (!uri.isOpaque()) {
            if (this.path.length() > 0 &&
                    !(this.path.charAt(this.path.length() - 1) == '/' || path.charAt(0) == '/')) {
                this.path.append('/');
            }
            this.path.append(path);
        }
        return this;
    }

    public static URIBuilder from(String uri) {
        return new URIBuilder(URI.create(uri));
    }

    public static URIBuilder from(URI uri) {
        return new URIBuilder(uri);
    }

    public URI getUri() {
        return uri;
    }

    public URI build() throws URISyntaxException {
        StringBuilder builder = new StringBuilder();
        if (uri.getScheme() != null) {
            builder.append(uri.getScheme());
            builder.append(":");
        }
        if (uri.isOpaque()) {
            builder.append(uri.getSchemeSpecificPart());
        } else {
            if (uri.getAuthority() != null) {
                builder.append("//");
                builder.append(uri.getAuthority());
            }
        }

        if (this.path.length() > 0) {
            if (builder.length() > 0 &&
                    !(builder.charAt(builder.length() - 1) == '/' || path.charAt(0) == '/')) {
                builder.append("/");
            }
            builder.append(path);
        }
        if (this.query.length() > 0) {
            builder.append("?");
            builder.append(query);
        }
        if (this.fragment.length() > 0) {
            builder.append("#");
            builder.append(fragment);
        }

        return new URI(builder.toString());
    }

}
