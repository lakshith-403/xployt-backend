package com.xployt.util;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {
  private static final Logger logger = CustomLogger.getLogger();
  private static final String URL = "jdbc:mysql://xployt-xployt.b.aivencloud.com:17847/xployt?ssl-mode=REQUIRED";
  private static final String USER = "avnadmin";
  private static final String PASSWORD = "AVNS_5G4ol30FyzBOm-NNf6x";

  public static Connection getConnection(ServletContext servletContext) throws SQLException {
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");
    if (conn == null || conn.isClosed() || !conn.isValid(2)) {
      conn = DriverManager.getConnection(URL, USER, PASSWORD);
      servletContext.setAttribute("DBConnection", conn);
    }
    return conn;
  }

  public static <T> T executeWithRetry(ServletContext servletContext, DatabaseOperation<T> operation)
      throws SQLException {
    int attempts = 0;
    while (attempts < 3) {
      try {
        Connection conn = getConnection(servletContext);
        return operation.execute(conn);
      } catch (SQLException e) {
        attempts++;
        logger.log(Level.WARNING, "Database operation failed (attempt " + attempts + "): " + e.getMessage(), e);
        if (attempts >= 3) {
          throw e;
        }
        try {
          Thread.sleep(2000); // Wait before retrying
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new SQLException("Interrupted during retry wait", ie);
        }
      }
    }
    throw new SQLException("Failed to execute database operation after 3 attempts");
  }

  @FunctionalInterface
  public interface DatabaseOperation<T> {
    T execute(Connection conn) throws SQLException;
  }
}