package net.novalab.webstart.service.uri.control;

import javax.ws.rs.core.PathSegment;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

    public String getPath() {
        return path.toString();
    }

    public String getQuery() {
        return query.toString();
    }

    public String getFragment() {
        return fragment.toString();
    }

    public URIBuilder addPathFromSource() {
        if (uri.getPath() != null) {
            addPath(uri.getPath());
        }
        return this;
    }

    public URIBuilder addParentPathFromSource() {
        if (uri.getPath() != null) {
            int index = uri.getPath().lastIndexOf('/');
            if (index >= 0) {
                addPath(uri.getPath().substring(0, index));
            }
        }
        return this;
    }

    public URIBuilder addQueryFromSource() {
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

    public URIBuilder addFragmentFromSource() {
        if (uri.getFragment() != null) {
            fragment.append(uri.getFragment());
        }
        return this;
    }

    public URIBuilder addPath(String path) {
        if (!uri.isOpaque() && path.length() > 0) {
            if (path.charAt(0) != '/' && (this.path.length() == 0
                    ? (uri.getScheme() != null || uri.getAuthority() != null)
                    : this.path.charAt(this.path.length() - 1) != '/')) {
                this.path.append('/');
            }
            this.path.append(path);
        }
        return this;
    }

    public static URIBuilder from(String uri) throws URISyntaxException {
        return new URIBuilder(new URI(uri));
    }

    public static URIBuilder from(URI uri) {
        return new URIBuilder(uri);
    }

    public static URIBuilder from(List<PathSegment> segments) throws URISyntaxException {
        StringBuilder sb = new StringBuilder();
        segments.stream()
                .map(PathSegment::getPath)
                .map("/"::concat)
                .forEach(sb::append);
        if (sb.charAt(sb.length() - 1) != '/' && !segments.get(segments.size() - 1).getPath().contains(".")) {
            sb.append("/");
        }
        return new URIBuilder(new URI(sb.toString()));
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
            if (uri.getScheme() != null || uri.getAuthority() != null) {
                builder.append("//");
            }
            if (uri.getAuthority() != null) {
                builder.append(uri.getAuthority());
            }
            if (this.path.length() > 0) {
                builder.append(path);
            }
            if (this.query.length() > 0) {
                builder.append("?");
                builder.append(query);
            }
        }

        if (this.fragment.length() > 0) {
            builder.append("#");
            builder.append(fragment);
        }

        return new URI(builder.toString());
    }

}
