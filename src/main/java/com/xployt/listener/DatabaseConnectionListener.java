package com.xployt.listener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import io.github.cdimascio.dotenv.Dotenv;

@WebListener
public class DatabaseConnectionListener implements ServletContextListener {
  private static final Logger logger = CustomLogger.getLogger();
  private static final Dotenv dotenv = Dotenv.load();
  private static final String URL = dotenv.get("DB_URL");
  private static final String USER = dotenv.get("DB_USER");
  private static final String PASSWORD = dotenv.get("DB_PASSWORD");
  private Connection connection;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    logger.info("DatabaseConnectionListener: Initializing");
    try {
      Class.forName("com.mysql.cj.jdbc.Driver"); // Optional for modern apps
      connection = DriverManager.getConnection(URL, USER, PASSWORD);

      ServletContext servletContext = sce.getServletContext();
      servletContext.setAttribute("DBConnection", connection);

      String contextName = "DBConnection";
      ContextManager.registerContext(contextName, servletContext);
      logger.log(Level.INFO, "DBConnection initialized and stored in ServletContext.");
    } catch (ClassNotFoundException e) {
      logger.log(Level.SEVERE, "MySQL driver not found: " + e.getMessage(), e);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to initialize database connection: " + e.getMessage(), e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    if (connection != null) {
      try {
        connection.close();
        logger.log(Level.INFO, "Database connection closed.");
        ContextManager.removeContext("DBConnection");
      } catch (SQLException e) {
        logger.log(Level.SEVERE, "Failed to close database connection: " + e.getMessage(), e);
      }
    }

    // Unregister JDBC driver
    try {
      Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
      while (drivers.hasMoreElements()) {
        java.sql.Driver driver = drivers.nextElement();
        DriverManager.deregisterDriver(driver);
        logger.log(Level.INFO, "Deregistering JDBC driver: " + driver);
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Error deregistering driver: " + e.getMessage(), e);
    }

    // Stop the abandoned connection cleanup thread
    try {
      com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
      logger.log(Level.INFO, "Abandoned connection cleanup thread stopped.");
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error stopping abandoned connection cleanup thread: ");
    }
  }
}