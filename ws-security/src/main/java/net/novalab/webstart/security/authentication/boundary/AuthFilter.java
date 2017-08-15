package net.novalab.webstart.security.authentication.boundary;

import jnlp.sample.servlet.JnlpDownloadServlet;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

/**
 * Created by ertunc on 31/08/15.
 */
@WebFilter(description = "Ensure authentication", urlPatterns = JnlpDownloadServlet.URL_PATTERN)
public class AuthFilter implements Filter {
    @Inject
    Principal principal;

    FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        config = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if ("anonymous".equals(principal.getName())) {
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"XS\"");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
