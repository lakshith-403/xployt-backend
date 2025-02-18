package com.xployt.dao.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

import com.xployt.model.ProjectBrief;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class ProjectsDAO {
  private static final Logger logger = CustomLogger.getLogger();

  public List<ProjectBrief> getAllProjects(String userId) {
    logger.info("Client ProjectsDAO: Inside getAllProjects");
    List<ProjectBrief> projects = new ArrayList<>();
    String sql = "SELECT * FROM Projects WHERE clientId = ?"; // Assuming a user_id column

    // Access the specific ServletContext by its name
    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");
    if (conn == null) {
      logger.severe("Client ProjectsDAO: Database connection is null");
      return projects;
    }
    logger.info("Client ProjectsDAO: Connection established");

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, userId);
      ResultSet rs = stmt.executeQuery();
      logger.info("Client ProjectsDAO: Fetching projects for user");
      while (rs.next()) {
        ProjectBrief project = new ProjectBrief(
            rs.getInt("projectId"),
            rs.getString("state"),
            rs.getString("title"),
            rs.getString("leadId"),
            rs.getString("clientId"),
            rs.getString("startDate"),
            rs.getString("endDate"),
            rs.getInt("pendingReports"));
        projects.add(project);
      }
      logger.info("Client ProjectsDAO: Projects fetched successfully");
      logger.info("Client ProjectsDAO: Number of projects fetched: " + projects.size());
    } catch (SQLException e) {
      logger.severe("Client ProjectsDAO: Error fetching projects: " + e.getMessage());
    }
    return projects;
  }
}