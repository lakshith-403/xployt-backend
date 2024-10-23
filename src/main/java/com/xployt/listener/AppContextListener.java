package com.xployt.listener;

import com.xployt.util.CustomLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public class AppContextListener implements ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      CustomLogger.setup();
      CustomLogger.getLogger().info("This should be blue");
      CustomLogger.getLogger().warning("This should be yellow");
      CustomLogger.getLogger().severe("This should be red");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // Cleanup code, if needed
  }
}