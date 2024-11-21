package com.xployt.dao.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletContext;
import com.xployt.util.CustomLogger;
import java.util.logging.Logger;
import com.xployt.util.ContextManager;

public class ProjectDAO {

  private static final Logger logger = CustomLogger.getLogger();

  public void createProject(String clientId, String projectTitle, String projectDescription, String startDate,
      String endDate, String url, String technicalStack) {

    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");

    String sql = "INSERT INTO Projects (clientId, title, description, startDate, endDate, url, technicalStack) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
      logger.info("sql: " + sql);
      preparedStatement.setString(1, clientId);
      preparedStatement.setString(2, projectTitle);
      preparedStatement.setString(3, projectDescription);
      preparedStatement.setString(4, startDate);
      preparedStatement.setString(5, endDate);
      preparedStatement.setString(6, url);
      preparedStatement.setString(7, technicalStack);
      logger.info("preparedStatement: " + preparedStatement);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      e.printStackTrace();
      // Handle exceptions appropriately
    }
  }
}