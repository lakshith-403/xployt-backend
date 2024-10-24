package com.xployt.listener;

import com.xployt.util.ContextManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class DatabaseConnectionListener implements ServletContextListener {
  private static final Logger logger = Logger.getLogger(DatabaseConnectionListener.class.getName());
  private static final String URL = "jdbc:mysql://xployt-xployt.b.aivencloud.com:17847/xployt?ssl-mode=REQUIRED";
  private static final String USER = "avnadmin";
  private static final String PASSWORD = "AVNS_nwmduVxzhIqTj911GGT";
  private Connection connection;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      connection = DriverManager.getConnection(URL, USER, PASSWORD);
      ServletContext servletContext = sce.getServletContext();
      servletContext.setAttribute("DBConnection", connection);

      String contextName = "DBConnection";
      ContextManager.registerContext(contextName, servletContext);
      logger.log(Level.INFO, "DBConnection initialized and stored in ServletContext.");

    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to initialize database connection: " + e.getMessage(), e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if (connection != null) {
      try {
        connection.close();
        ContextManager.removeContext("DBConnection");
        logger.log(Level.INFO, "Database connection closed.");
      } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to close database connection: " + e.getMessage(), e);
      } finally {
        try {
          DriverManager.deregisterDriver(DriverManager.getDriver(URL));
        } catch (SQLException e) {
          logger.log(Level.SEVERE, "Failed to deregister driver: " + e.getMessage(), e);
        }
      }
    }
  }
}
