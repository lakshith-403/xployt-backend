package com.xployt.dao.lead;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

import com.xployt.model.ProjectBriefLead;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class ProjectsDAO {
  private static final Logger logger = CustomLogger.getLogger();

  public List<ProjectBriefLead> getAllProjects(String userId) {
    logger.info("Lead ProjectsDAO: Inside getAllProjects");
    List<ProjectBriefLead> projects = new ArrayList<>();
    String sql = "SELECT * FROM Projects WHERE leadId = ?"; // Assuming a user_id column

    // Access the specific ServletContext by its name
    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");
    logger.info("Lead ProjectsDAO: Connection established");

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, userId);
      ResultSet rs = stmt.executeQuery();
      logger.info("Lead ProjectsDAO: Fetching projects for user");
      while (rs.next()) {
        ProjectBriefLead project = new ProjectBriefLead(
            rs.getInt("projectId"),
            rs.getString("status"),
            rs.getString("title"),
            rs.getString("clientId"),
            rs.getInt("pendingReports"));
        projects.add(project);
      }
      logger.info("Lead ProjectsDAO: Projects fetched successfully");
      logger.info("Lead ProjectsDAO: Number of projects fetched: " + projects.size());
    } catch (SQLException e) {
      logger.severe("Lead ProjectsDAO: Error fetching projects: " + e.getMessage());
    }
    return projects;
  }
}
