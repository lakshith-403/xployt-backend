package com.xployt.controller.lead;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.sql.SQLException;
import java.util.stream.Collectors;

import com.xployt.dao.common.NotificationDAO;
import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.model.ProjectTeam;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

/**
 * Servlet for handling project configuration requests.
 * 
 * This servlet handles the configuration of projects, including the assignment
 * of validators.
 * It provides functionality for inserting project scopes, payment levels,
 * payment level amounts,
 * and project configurations.
 */
@WebServlet("/api/lead/project/*")
public class ProjectConfigServlet extends HttpServlet {

  private Map<String, Object> requestBody;

  /**
   * Handles GET requests for project configuration.
   * 
   * This method retrieves project and user information based on the provided
   * project ID.
   * It executes a combined SQL query to fetch the project details along with the
   * associated user.
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ ProjectConfigServlet | doGet ------------");

    try {
      List<String> pathParams = RequestProtocol.parsePathParams(request);

      if (!pathParams.isEmpty()) {

        // Combined SQL Statement
        String sqlCombined = "SELECT p.*, u.* FROM Projects p JOIN Users u ON p.clientId = u.userId WHERE p.projectId = ?;";

        // Execute Combined Query
        List<Object[]> sqlParams = new ArrayList<>();
        sqlParams.add(new Object[] { pathParams.get(0) });
        List<Map<String, Object>> combinedResults = DatabaseActionUtils.executeSQL(
            new String[] { sqlCombined }, sqlParams);

        if (!combinedResults.isEmpty()) {
          ResponseProtocol.sendSuccess(request, response, this, "Project and User found",
              combinedResults.get(0), HttpServletResponse.SC_OK);
          return;
        } else {
          ResponseProtocol.sendError(request, response, this, "Project or User not found",
              "Project or User not found", HttpServletResponse.SC_NOT_FOUND);
          return;
        }
      }

      ResponseProtocol.sendError(request, response, this, "Path params is empty",
          Map.of("pathParams", new ArrayList<>()), HttpServletResponse.SC_BAD_REQUEST);

    } catch (Exception e) {
      System.out.println("Error getting project config info: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error getting project config info",
          e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles POST requests for project configuration.
   * 
   * This method processes the request body to extract project configuration data,
   * including project scopes, payment levels, payment level amounts, and project
   * configurations.
   * 
   **/
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ ProjectConfigServlet | doPost ------------");

    try {
      requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      String projectId = (String) requestBody.get("projectId");

      String update = (String) RequestProtocol.parseQueryParams(request).get("update");

      if (update != null && !update.isEmpty()) {
        if (update.equals("true")) {
          cleanupProjectData(Integer.parseInt(projectId));
        }
      }

      // Process testingScope with batch processing
      String testingScope = (String) requestBody.get("testingScope");
      if (testingScope != null && !testingScope.isEmpty()) {
        // Collect unique, trimmed scope items using streams
        Set<String> trimmedScopeItems = Arrays.stream(testingScope.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());

        // Retrieve all scope IDs in one database round trip
        Map<String, Integer> scopeIdMap = getScopeIds(new ArrayList<>(trimmedScopeItems));
        List<Object[]> projectScopeBatchParams = new ArrayList<>();

        for (String scopeItem : trimmedScopeItems) {
          Integer scopeId = scopeIdMap.get(scopeItem);
          if (scopeId == null) {
            ResponseProtocol.sendError(request, response, this, "Scope item not found: " + scopeItem,
                Map.of("missingScopeItem", scopeItem), HttpServletResponse.SC_BAD_REQUEST);
            return;
          }
          projectScopeBatchParams.add(new Object[] { scopeId, Integer.parseInt(projectId) });
        }
        // Batch insert project scopes
        insertProjectScopesBatch(projectScopeBatchParams);
      }

      // Process PaymentLevels with batch insertion
      String[] levels = { "critical", "high", "medium", "low", "informative" };
      List<Object[]> paymentLevelParams = new ArrayList<>();

      for (String level : levels) {
        String value = (String) requestBody.get(level);
        if (value != null && !value.isEmpty()) {
          for (String item : value.split(",")) {
            item = item.trim();
            if (!item.isEmpty()) {
              paymentLevelParams.add(new Object[] { Integer.parseInt(projectId), level, item });
            }
          }
        }
      }
      // Remove duplicates if any and execute batch insert
      paymentLevelParams = new ArrayList<>(new HashSet<>(paymentLevelParams));
      insertPaymentLevelsBatch(paymentLevelParams);

      // Process PaymentLevelAmounts with batch insertion
      String[] fundingFields = { "criticalFunding", "highFunding", "mediumFunding", "lowFunding",
          "informativeFunding" };
      List<Object[]> paymentLevelAmountParams = new ArrayList<>();

      for (int i = 0; i < fundingFields.length; i++) {
        String fundingField = fundingFields[i];
        if (requestBody.get(fundingField) != null) {
          Object fundingValueObj = requestBody.get(fundingField);
          String fundingValue;
          if (fundingValueObj instanceof Double) {
            fundingValue = Double.toString((Double) fundingValueObj);
          } else {
            fundingValue = (String) fundingValueObj;
          }
          if (fundingValue != null && !fundingValue.isEmpty()) {
            // System.out.println("Funding value: " + fundingValue);
            paymentLevelAmountParams.add(new Object[] { Integer.parseInt(projectId), levels[i],
                fundingValue });
          }
        }
      }
      paymentLevelAmountParams = new ArrayList<>(new HashSet<>(paymentLevelAmountParams));
      insertPaymentLevelAmountsBatch(paymentLevelAmountParams);

      // Process project configuration data
      String outOfScope = (String) requestBody.get("outOfScope");
      String objectives = (String) requestBody.get("objectives");
      String securityRequirements = (String) requestBody.get("securityRequirements");

      // Get hacker count if available, default to 0
      int noOfHackers = 0;
      if (requestBody.get("noOfHackers") != null) {
        Object hackersObj = requestBody.get("noOfHackers");
        if (hackersObj instanceof Double) {
          noOfHackers = ((Double) hackersObj).intValue();
        } else if (hackersObj instanceof String) {
          noOfHackers = Integer.parseInt((String) hackersObj);
        }
      }

      // Calculate validator count as ceiling of square root of hacker count
      int noOfValidators = (int) Math.ceil(Math.sqrt(noOfHackers));

      // Get initial funding if available
      String initialFunding = null;
      if (requestBody.get("initialFunding") != null) {
        Object fundingObj = requestBody.get("initialFunding");
        if (fundingObj instanceof Double) {
          initialFunding = Double.toString((Double) fundingObj);
        } else {
          initialFunding = (String) fundingObj;
        }
      }

      // Insert into ProjectConfigs table
      String configSql = "INSERT INTO ProjectConfigs (projectId, outOfScope, objectives, securityRequirements, initialFunding, noOfHackers, noOfValidators) VALUES (?, ?, ?, ?, ?, ?, ?)";
      List<Object[]> configParams = new ArrayList<>();
      configParams.add(new Object[] {
          Integer.parseInt(projectId),
          outOfScope,
          objectives,
          securityRequirements,
          initialFunding,
          noOfHackers,
          noOfValidators
      });

      DatabaseActionUtils.executeSQL(new String[] { configSql }, configParams);

      ResponseProtocol.sendSuccess(request, response, this, "Data inserted successfully",
          Map.of("projectId", projectId), HttpServletResponse.SC_CREATED);

      String sqlStatement = "UPDATE Projects SET state = 'Configured' WHERE projectId = ?";
      List<Object[]> sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { Integer.parseInt(projectId) });
      DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, sqlParams);

      //      Notification
      ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
      ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
      NotificationDAO notificationDAO = new NotificationDAO();
      notificationDAO.createNotification(
              projectTeam.getProjectLead().getUserId(),
              "Project #" + projectId,
              "Client " + projectTeam.getClient().getName() + " has configured the project",
              "/projects/" + projectId
      );

    } catch (Exception e) {
      System.out.println("Error processing request: " + e.getMessage());
      // Attempt to rollback any inserted data if projectId is available
      if (requestBody != null && requestBody.get("projectId") != null) {
        String projectIdStr = (String) requestBody.get("projectId");
        cleanupProjectData(Integer.parseInt(projectIdStr));
      }
      ResponseProtocol.sendError(request, response, this, "Error processing request",
          e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private Map<String, Integer> getScopeIds(List<String> scopeNames) throws SQLException {
    if (scopeNames.isEmpty()) {
      return Collections.emptyMap();
    }
    // Build placeholders for the IN clause using streams
    String placeholders = scopeNames.stream()
        .map(s -> "?")
        .collect(Collectors.joining(","));
    String selectSql = "SELECT scopeName, scopeId FROM scopeItems WHERE scopeName IN (" + placeholders + ")";

    List<Object[]> selectParams = new ArrayList<>();
    // Add all scopeNames as the parameters for the query
    selectParams.add(scopeNames.toArray());

    List<Map<String, Object>> results = DatabaseActionUtils.executeSQL(
        new String[] { selectSql }, selectParams);
    Map<String, Integer> scopeIdMap = new HashMap<>();
    for (Map<String, Object> result : results) {
      scopeIdMap.put((String) result.get("scopeName"), (Integer) result.get("scopeId"));
    }
    return scopeIdMap;
  }

  private void insertProjectScopesBatch(List<Object[]> batchParams) throws SQLException {
    String insertSql = "INSERT INTO ProjectScope (scopeId, projectId) VALUES (?, ?)";
    DatabaseActionUtils.executeBatchSQL(insertSql, batchParams);
  }

  private void insertPaymentLevelsBatch(List<Object[]> batchParams) throws SQLException {
    String sql = "INSERT INTO PaymentLevels (projectId, level, item) VALUES (?, ?, ?)";
    DatabaseActionUtils.executeBatchSQL(sql, batchParams);
  }

  private void insertPaymentLevelAmountsBatch(List<Object[]> batchParams) throws SQLException {
    String sql = "INSERT INTO PaymentLevelAmounts (projectId, level, amount) VALUES (?, ?, ?)";
    DatabaseActionUtils.executeBatchSQL(sql, batchParams);
  }

  private void cleanupProjectData(int projectId) {
    try {
      String deletePaymentLevelAmountsSql = "DELETE FROM PaymentLevelAmounts WHERE projectId = ?";
      DatabaseActionUtils.executeSQL(
          new String[] { deletePaymentLevelAmountsSql },
          Collections.singletonList(new Object[] { projectId }));

      String deletePaymentLevelsSql = "DELETE FROM PaymentLevels WHERE projectId = ?";
      DatabaseActionUtils.executeSQL(
          new String[] { deletePaymentLevelsSql },
          Collections.singletonList(new Object[] { projectId }));

      String[] deleteProjectScopeSql = { "DELETE FROM ProjectScope WHERE projectId = ?",
          "UPDATE Projects SET state = 'Unconfigured' WHERE projectId = ?" };
      List<Object[]> sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { projectId });
      sqlParams.add(new Object[] { projectId });
      DatabaseActionUtils.executeSQL(deleteProjectScopeSql, sqlParams);

      String deleteProjectConfigsSql = "DELETE FROM ProjectConfigs WHERE projectId = ?";
      DatabaseActionUtils.executeSQL(
          new String[] { deleteProjectConfigsSql },
          Collections.singletonList(new Object[] { projectId }));
    } catch (SQLException ex) {
      System.err.println("Cleanup failed for projectId " + projectId + ": " + ex.getMessage());
    }
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ ProjectConfigServlet | doDelete ------------");

    try {
      String projectId = RequestProtocol.parsePathParams(request).get(0);
      System.out.println("Project ID: " + projectId);
      System.out.println("Project Closed");

      String sqlStatement = "UPDATE Projects SET state = 'Closed' WHERE projectId = ?";
      List<Object[]> sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { Integer.parseInt(projectId) });
      DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, sqlParams);

      NotificationDAO notificationDAO = new NotificationDAO();
      notificationDAO.sendNotificationsToProjectTeam(
              Integer.parseInt(projectId),
              "Project #" + projectId + " has been closed by Project Lead."
      );

      ResponseProtocol.sendSuccess(request, response, this, "Project closed successfully",
          Map.of("projectId", projectId), HttpServletResponse.SC_OK);
    } catch (Exception e) {
      System.out.println("Error closing project: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error closing project", e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
