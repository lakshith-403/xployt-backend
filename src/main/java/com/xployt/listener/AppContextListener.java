package com.xployt.listener;

import com.xployt.util.CustomLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

  private Logger logger;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      CustomLogger.setup();
      this.logger = CustomLogger.getLogger();
      logger.info("Logging system initialized");
      logger.info("This is an INFO message");
      logger.severe("This is a SEVERE message");
      logger.warning("This is a WARNING message");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cleanup code, if needed
  }
}