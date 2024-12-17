package com.xployt.dao.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import com.xployt.util.CustomLogger;
import java.util.logging.Logger;
import com.xployt.util.ContextManager;
import java.sql.ResultSet;

public class ProjectDAO {

  private static final Logger logger = CustomLogger.getLogger();

  public int createProject(int clientId, String projectTitle, String projectDescription, String startDate,
      String endDate, String url, String technicalStack, Connection conn) {

    if (conn == null) {
      ServletContext servletContext = ContextManager.getContext("DBConnection");
      conn = (Connection) servletContext.getAttribute("DBConnection");
    }

    String sql = "INSERT INTO Projects (clientId, title, description, startDate, endDate, url, technicalStack) VALUES (?, ?, ?, ?, ?, ?, ?)";
    String getProjectId = "SELECT projectId FROM Projects WHERE clientId = ? AND title = ? AND description = ? AND startDate = ? AND endDate = ? AND url = ? AND technicalStack = ?";

    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
      // logger.info("sql: " + sql);
      preparedStatement.setInt(1, clientId);
      preparedStatement.setString(2, projectTitle);
      preparedStatement.setString(3, projectDescription);
      preparedStatement.setString(4, startDate);
      preparedStatement.setString(5, endDate);
      preparedStatement.setString(6, url);
      preparedStatement.setString(7, technicalStack);
      // logger.info("preparedStatement: " + preparedStatement);
      preparedStatement.executeUpdate();

    } catch (SQLException e) {
      logger.severe("Error creating project: " + e.getMessage());
      // e.printStackTrace();
      // Handle exceptions appropriately
    }
    try (PreparedStatement preparedStatement = conn.prepareStatement(getProjectId)) {
      preparedStatement.setInt(1, clientId);
      preparedStatement.setString(2, projectTitle);
      preparedStatement.setString(3, projectDescription);
      preparedStatement.setString(4, startDate);
      preparedStatement.setString(5, endDate);
      preparedStatement.setString(6, url);
      preparedStatement.setString(7, technicalStack);
      ResultSet rs = preparedStatement.executeQuery();
      if (rs.next()) {
        return rs.getInt("projectId");
      }
    } catch (SQLException e) {
      System.out.println("Error getting project id: " + e.getMessage());
    }
    return -1;
  }
}