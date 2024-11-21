package com.xployt.middleware;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;
// import java.util.stream.Collectors;

public class RequestLoggingFilter implements Filter {

    private static final Logger logger = Logger.getLogger(RequestLoggingFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);

            String method = wrappedRequest.getMethod();
            String requestURI = wrappedRequest.getRequestURI();
            String queryString = wrappedRequest.getQueryString();
            String fullURL = requestURI + (queryString != null ? "?" + queryString : "");

            logger.info("Incoming request: " + method + " " + fullURL + "\n");

            // if ("POST".equalsIgnoreCase(method)) {
            // // Log the body of the POST request
            // String requestBody = wrappedRequest.getReader().lines()
            // .collect(Collectors.joining(System.lineSeparator()));
            // logger.info("Request body: " + requestBody);
            // }

            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Cleanup code, if needed
    }
}