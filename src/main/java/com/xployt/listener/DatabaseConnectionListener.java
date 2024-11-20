package com.xployt.listener;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class DatabaseConnectionListener implements ServletContextListener {
  private static final Logger logger = CustomLogger.getLogger();
  private static final String URL = "jdbc:mysql://xployt-xployt.b.aivencloud.com:17847/xployt?ssl-mode=REQUIRED";
  private static final String USER = "avnadmin";
  private static final String PASSWORD = "AVNS_5G4ol30FyzBOm-NNf6x";
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
  }
}