package com.blinky.apillama3blinky.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PathNormalizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // Check if the path starts with "api/" instead of "/api/" or if it's exactly "events" or starts with "events/"
        if (requestURI.startsWith("api/") || requestURI.equals("events") || requestURI.startsWith("events/")) {
            // Determine the normalized path
            String normalizedPath;
            if (requestURI.equals("events") || requestURI.startsWith("events/")) {
                // If it's "events" or "events/...", prepend "/api/"
                normalizedPath = "/api/" + requestURI;
            } else {
                // If it's "api/...", just prepend "/"
                normalizedPath = "/" + requestURI;
            }

            // Wrap the request to normalize the path
            final String finalNormalizedPath = normalizedPath;
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getRequestURI() {
                    return finalNormalizedPath;
                }

                @Override
                public String getServletPath() {
                    return finalNormalizedPath;
                }
            };

            // Continue with the wrapped request
            chain.doFilter(wrappedRequest, response);
        } else {
            // Continue with the original request
            chain.doFilter(request, response);
        }
    }
}
