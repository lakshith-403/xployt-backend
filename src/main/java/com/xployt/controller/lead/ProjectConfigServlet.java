package com.xployt.controller.lead;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.SQLException;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

// import com.xployt.service.lead.ProjectService;

@WebServlet("/api/lead/project/*")
public class ProjectConfigServlet extends HttpServlet {

  // private final ProjectService projectService = new ProjectService();
  // @Override
  // protected void doGet(HttpServletRequest request, HttpServletResponse
  // response) {
  // System.out.println("ProjectConfigServlet doGet method called");
  // // projectService.getProjectConfigInfo(request, response);
  // // response.setContentType("application/json");
  // // response.setCharacterEncoding("UTF-8");
  // }
  // @Override
  // protected void doPost(HttpServletRequest request, HttpServletResponse
  // response) {
  // System.out.println("ProjectConfigServlet doPost method called");
  // // projectService.updateProjectConfigInfo(request, response);
  // // response.setContentType("application/json");
  // // response.setCharacterEncoding("UTF-8");
  // }
  // }

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ ProjectConfigServlet | doGet ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);

      if (pathParams.size() > 0) {

        // Combined SQL Statement
        String sqlCombined = "SELECT p.*, u.* FROM Projects p JOIN Users u ON p.clientId = u.userId WHERE p.projectId = ?;";

        // Execute Combined Query
        sqlParams.clear();
        sqlParams.add(new Object[] { pathParams.get(0) });
        List<Map<String, Object>> combinedResults = DatabaseActionUtils.executeSQL(new String[] { sqlCombined },
            sqlParams);

        // System.out.println("Combined results: " + combinedResults);

        // Check Results
        if (!combinedResults.isEmpty()) {
          // Map<String, Object> mergedResults = new HashMap<>();
          // mergedResults.put("project", combinedResults.get(0)); // Project details
          // mergedResults.put("user", combinedResults.get(0)); // User details (same row
          // due to JOIN)
          // System.out.println("Merged results: " + mergedResults);
          ResponseProtocol.sendSuccess(request, response, this, "Project and User found",
              combinedResults.get(0),
              HttpServletResponse.SC_OK);
          return;
        } else {
          ResponseProtocol.sendError(request, response, this, "Project or User not found",
              "Project or User not found",
              HttpServletResponse.SC_NOT_FOUND);
          return;
        }

      }

      ResponseProtocol.sendError(request, response, this, "Path params is empty",
          Map.of("pathParams", new ArrayList<>()),
          HttpServletResponse.SC_BAD_REQUEST);

    } catch (Exception e) {
      System.out.println("Error getting project config info: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error getting project config info", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ ProjectConfigServlet | doPost ------------");

    try {
      Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      String projectId = (String) requestBody.get("projectId");

      // Define the fields to process for PaymentLevels
      String[] levels = { "critical", "high", "medium", "low", "informative" };

      for (String field : levels) {
        String value = (String) requestBody.get(field);
        if (value != null && !value.isEmpty()) {
          String[] items = value.split(",");
          for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) {
              insertIntoPaymentLevels(projectId, field, item);
            }
          }
        }
      }
      System.out.println("Payment levels inserted successfully");
      // Define the levels to process for PaymentLevelAmounts
      String[] fundingFields = { "criticalFunding", "highFunding", "mediumFunding", "lowFunding",
          "informativeFunding" };

      for (int i = 0; i < fundingFields.length; i++) {
        Double fundingValue = (Double) requestBody.get(fundingFields[i]);
        if (fundingValue != null) {
          insertIntoPaymentLevelAmounts(projectId, levels[i], fundingValue);
        }
      }

      ResponseProtocol.sendSuccess(request, response, this, "Data inserted successfully",
          Map.of("projectId", projectId), HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error processing request: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error processing request", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void insertIntoPaymentLevels(String projectId, String level, String item) throws SQLException {
    String sql = "INSERT INTO PaymentLevels (projectId, level, area) VALUES (?, ?, ?)";
    List<Object[]> sqlParams = new ArrayList<>();
    sqlParams.add(new Object[] { Integer.parseInt(projectId), level, item });

    DatabaseActionUtils.executeSQL(new String[] { sql }, sqlParams);
  }

  private void insertIntoPaymentLevelAmounts(String projectId, String level, double amount) throws SQLException {
    String sql = "INSERT INTO PaymentLevelAmounts (projectId, level, amount) VALUES (?, ?, ?)";
    List<Object[]> sqlParams = new ArrayList<>();
    sqlParams.add(new Object[] { Integer.parseInt(projectId), level, amount });

    DatabaseActionUtils.executeSQL(new String[] { sql }, sqlParams);
  }

}