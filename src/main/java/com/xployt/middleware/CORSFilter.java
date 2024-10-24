package com.xployt.middleware;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.xployt.util.CustomLogger;
import java.util.logging.Logger;

public class CORSFilter implements Filter {
  Logger logger = CustomLogger.getLogger();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization code, if needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    logger.info("Adding CORS headers");
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    String origin = httpRequest.getHeader("Origin");
    if (origin != null) {
      httpResponse.setHeader("Access-Control-Allow-Origin", origin);
    }
    httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

    if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
      // If it's a preflight request, just return without processing the request
      // further.
      logger.info("Preflight request detected. Sending 200 OK response.");
      httpResponse.setStatus(HttpServletResponse.SC_OK);
      return;
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    // Cleanup code, if needed
  }
}