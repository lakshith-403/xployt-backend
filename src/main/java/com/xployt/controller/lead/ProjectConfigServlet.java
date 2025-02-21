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
      System.out.println("Path params: " + pathParams);

      if (pathParams.size() > 0) {
        System.out.println("Path params: " + pathParams.get(0));

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
    System.out.println("\n\n------------ ProjectConfigServlet | doPost ------------");

    int userId = 0;
    try {
      requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);
      // Simulate requestBody

      sqlStatements = new String[] {
          "INSERT INTO Users (email, passwordHash, name, role, status) VALUES (?, ?, ?, 'Validator', 'inactive')",
          "UPDATE Users SET role = ? WHERE email = ?",
          "UPDATE Users SET name = ? WHERE email = ?",
          "SELECT userId FROM Users WHERE email = ?"

      };

      sqlParams.clear();

      sqlParams.add(new Object[] { "Test@test.com",
          "testPassword",
          "Test User" });

      sqlParams.add(new Object[] { "Admin", "Test@test.com" });

      sqlParams.add(new Object[] { "Edited Test User", "Test@test.com" });

      sqlParams.add(new Object[] { "Test@test.com" });

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      if (results.size() > 0) {
        System.out.println("After rs.next()");
        userId = (int) results.get(0).get("userId");
        System.out.println("User ID: " + userId);
      }

      sqlStatements = new String[] {
          "DELETE FROM Users WHERE userId = ?"
      };
      sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { userId });
      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      System.out.println("Test case done successfully");

      ResponseProtocol.sendSuccess(request, response, this, "Test case excuted successfully",
          Map.of("userId", userId),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error creating validator: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error creating validator", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ ProjectConfigServlet | doPut ------------");
    queryParams = RequestProtocol.parseQueryParams(request);
    System.out.println("Query params: " + queryParams);

    if (queryParams.size() > 0) {
      ResponseProtocol.sendSuccess(request, response, this, "Query params is not empty",
          Map.of("queryParams", queryParams),
          HttpServletResponse.SC_OK);
      return;
    }

    ResponseProtocol.sendError(request, response, this, "Query params is empty",
        Map.of("queryParams", new HashMap<>()),
        HttpServletResponse.SC_BAD_REQUEST);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ ProjectConfigServlet | doDelete ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);
  }
}