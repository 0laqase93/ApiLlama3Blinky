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

        if (requestURI.startsWith("api/") || requestURI.equals("events") || requestURI.startsWith("events/")) {
            String normalizedPath;
            if (requestURI.equals("events") || requestURI.startsWith("events/")) {
                normalizedPath = "/api/" + requestURI;
            } else {
                normalizedPath = "/" + requestURI;
            }

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

            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
