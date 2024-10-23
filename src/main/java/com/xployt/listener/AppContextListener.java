package com.xployt.listener;

import com.xployt.util.CustomLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

  private static final Logger logger = CustomLogger.getLogger();

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      CustomLogger.setup();
      logger.info("Logging system initialized");

      // Example log statements at different levels
      logger.severe("This is a SEVERE message");
      logger.warning("This is a WARNING message");
      logger.info("This is an INFO message");
      logger.config("This is a CONFIG message");
      logger.fine("This is a FINE message");
      logger.finer("This is a FINER message");
      logger.finest("This is a FINEST message");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cleanup code, if needed
  }
}