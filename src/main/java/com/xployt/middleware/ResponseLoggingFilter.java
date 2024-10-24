package com.xployt.middleware;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class ResponseLoggingFilter implements Filter {
  private static final Logger logger = Logger.getLogger(ResponseLoggingFilter.class.getName());

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization code, if needed
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      CustomHttpServletResponseWrapper responseWrapper = new CustomHttpServletResponseWrapper(
          (HttpServletResponse) response);

      chain.doFilter(request, responseWrapper);

      String responseContent = responseWrapper.getContent();
      logger.info("Outgoing response: " + responseContent);

      // Write the captured content to the actual response
      response.getWriter().write(responseContent);
    } else {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {
    // Cleanup code, if needed
  }
}