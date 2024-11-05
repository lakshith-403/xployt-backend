package com.xployt.listener;

import com.xployt.middleware.CORSFilter;
import com.xployt.middleware.RequestLoggingFilter;
// import com.xployt.middleware.ResponseLoggingFilter;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;

@WebListener
public class FilterOrderingListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      ServletContext servletContext = sce.getServletContext();

      // Register CORSFilter first
      FilterRegistration.Dynamic corsFilter = servletContext.addFilter("CORSFilter", CORSFilter.class);
      corsFilter.addMappingForUrlPatterns(null, false, "/*");

      // Register RequestLoggingFilter second
      FilterRegistration.Dynamic loggingFilter = servletContext.addFilter("RequestLoggingFilter",
          RequestLoggingFilter.class);
      loggingFilter.addMappingForUrlPatterns(null, false, "/*");

      // Register ResponseLoggingFilter last
      // FilterRegistration.Dynamic responseLoggingFilter =
      // servletContext.addFilter("ResponseLoggingFilter",
      // ResponseLoggingFilter.class);
      // responseLoggingFilter.addMappingForUrlPatterns(null, false, "/*");

    } catch (Exception e) {
      System.err.println("Error registering filters: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cleanup code, if needed
  }
}