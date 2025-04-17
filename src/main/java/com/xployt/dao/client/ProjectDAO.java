package com.xployt.dao.client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;
import java.sql.ResultSet;
// import com.xployt.util.SQLLoader;
import com.xployt.model.Project;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletContext;

public class ProjectDAO {

  private static final Logger logger = CustomLogger.getLogger();

  /**
   * Method to create a project from JSON data
   * Only initial project data from client request is entered into the database
   * 
   * @param clientId
   * @param projectTitle
   * @param projectDescription
   * @param startDate
   * @param endDate
   * @param url
   * @param technicalStack
   * @param conn
   * @return projectId
   */
  public int createProject(int clientId, String projectTitle, String projectDescription, String startDate,
      String endDate, String url, String technicalStack, Connection conn) throws Exception {

    String sql = "INSERT INTO Projects (clientId, title, description, startDate, endDate, url, technicalStack) VALUES (?, ?, ?, ?, ?, ?, ?)";
    System.out.println("clientId: " + clientId);
    System.out.println("projectTitle: " + projectTitle);
    System.out.println("projectDescription: " + projectDescription);
    System.out.println("startDate: " + startDate);
    System.out.println("endDate: " + endDate);
    System.out.println("url: " + url);
    System.out.println("technicalStack: " + technicalStack);
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

    } catch (Exception e) {
      logger.severe("Error creating project: " + e.getMessage());
      System.out.println("Inside createProject DAO");
      e.printStackTrace();
      // Handle exceptions appropriately
    }
    String getProjectId = "SELECT projectId FROM Projects WHERE clientId = ? AND title = ? AND description = ? AND startDate = ? AND endDate = ? AND url = ? AND technicalStack = ?";
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
    } catch (Exception e) {
      System.out.println("Error getting project id: " + e.getMessage());
    }
    return -1;
  }

  /**
   * Method to assign a project lead to a project
   * 
   * @param projectId
   * @param conn
   */
  public int assignProjectLead(int projectId, Connection conn) throws Exception {
    int leadId = -1;
    try {
      leadId = getSuitedLead(projectId, conn);
      System.out.println("leadId: " + leadId);
      System.out.println("projectId: " + projectId);
      if (leadId == -1) {
        logger.severe("No suitable lead found for projectId: " + projectId);
        throw new Exception("No suitable lead found for projectId: " + projectId);
      }

      String sql = "UPDATE Projects SET leadId = ? WHERE projectId = ?";
      PreparedStatement preparedStatement = conn.prepareStatement(sql);
      preparedStatement.setInt(1, leadId);
      preparedStatement.setInt(2, projectId);
      preparedStatement.executeUpdate();

      String updateProjectLeadInfo = "UPDATE ProjectLeadInfo SET activeProjectCount = activeProjectCount + 1 WHERE projectLeadId = ?";
      PreparedStatement preparedStatement3 = conn.prepareStatement(updateProjectLeadInfo);
      preparedStatement3.setInt(1, leadId);
      preparedStatement3.executeUpdate();

    } catch (Exception e) {
      logger.severe("Error assigning project lead: " + e.getMessage());
      throw new Exception("Error assigning project lead: " + e.getMessage());
    }
    return leadId;
  }

  /**
   * Method to get a suitable lead for a project using a scoring algorithm.
   * The algorithm calculates a combined score based on:
   * 1. Active project score (60% weight): Favors leads with fewer active projects
   * 2. Total project score (40% weight): Favors leads with fewer total projects
   * Both scores are normalized against the maximum count in their category.
   * The lead with the highest combined score is selected.
   * 
   * @param projectId The ID of the project needing a lead
   * @param conn      Database connection
   * @return leadId ID of the most suitable project lead, or -1 if none found
   */

  int getSuitedLead(int projectId, Connection conn) {
    // String sql = "";
    // try {
    // // sql = SQLLoader.loadSQL("getSuitedLead.sql");
    // } catch (Exception e) {
    // logger.severe("Error reading SQL file: " + e.getMessage());
    // }

    String sql = "WITH MaxCounts AS ("
        + " SELECT "
        + " MAX(activeProjectCount) + 1 AS max_active_projects,"
        + " MAX(completedProjectCount) + 1 AS max_completed_projects"
        + " FROM ProjectLeadInfo"
        + ")"
        + ""
        + "SELECT "
        + " u.userId,"
        + " "
        + " ((1 - COALESCE(pli.activeProjectCount, 0) / mc.max_active_projects) * 0.6 + "
        + " (1 - COALESCE(pli.completedProjectCount, 0) / mc.max_completed_projects) * 0.4) AS combined_score"
        + " FROM Users u"
        + " LEFT JOIN ProjectLeadInfo pli ON u.userId = pli.projectLeadId"
        + " CROSS JOIN MaxCounts mc"
        + " WHERE u.role = 'ProjectLead'"
        + " ORDER BY combined_score DESC"
        + " LIMIT 1";
    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
      ResultSet rs = preparedStatement.executeQuery();
      if (rs.next()) {
        return rs.getInt("userId");
      }
    } catch (Exception e) {
      logger.severe("Error getting suited lead: " + e.getMessage());
    }
    return -1;
  }

  public Project closeProject(int projectId){
    String sql = "UPDATE Projects SET state = 'CLOSED' WHERE projectId = ?";
    ServletContext servletContext = ContextManager.getContext("DBConnection");
    Connection conn = (Connection) servletContext.getAttribute("DBConnection");
    try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
      preparedStatement.setInt(1, projectId);
      preparedStatement.executeUpdate();
    } catch (Exception e) {
      logger.severe("Error closing project: " + e.getMessage());
    }
    return null;
  }
}