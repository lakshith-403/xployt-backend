package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

/*
 * Provides data to populate diagrams, tables and stuff in dashboards of all users
 */
@WebServlet("/api/dashboard/*")
public class DashboardStatsServlet extends HttpServlet {

  private static String[] SQL_STATEMENTS = {};
  private static List<Object[]> SQL_PARAMS = new ArrayList<>();
  private static final List<Map<String, Object>> RESULTS = new ArrayList<>();
  private static Map<String, Object> REQUEST_BODY = new HashMap<>();
  private static ArrayList<String> PATH_PARAMS = new ArrayList<>();
  private static Map<String, Object> QUERY_PARAMS = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ DashboardStatsServlet | doGet ------------");

    try {
      PATH_PARAMS.clear();
      PATH_PARAMS.addAll(RequestProtocol.parsePathParams(request));
      System.out.println("Path params: " + PATH_PARAMS);

      if (!PATH_PARAMS.isEmpty()) {
        System.out.println("Path params: " + PATH_PARAMS.get(0));
        handlePathParams(request, response);
        return;
      }

      ResponseProtocol.sendError(request, response, this, "Path params is empty",
          Map.of("pathParams", new ArrayList<>()),
          HttpServletResponse.SC_BAD_REQUEST);

    } catch (Exception e) {
      ResponseProtocol.sendError(request, response, this, e.getMessage(),
          Map.of("error", e.getMessage()),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void handlePathParams(HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException {
    if ("project-stats".equals(PATH_PARAMS.get(0)) && PATH_PARAMS.size() == 3) {
      RESULTS.clear();
      RESULTS.addAll(getProjectStats(PATH_PARAMS.get(1), PATH_PARAMS.get(2)));
    } else if ("recent-projects".equals(PATH_PARAMS.get(0)) && PATH_PARAMS.size() == 3) {
      RESULTS.clear();
      RESULTS.addAll(getRecentProjects(PATH_PARAMS.get(1), PATH_PARAMS.get(2)));
    }

    ResponseProtocol.sendSuccess(request, response, this, "Project stats fetched successfully",
        Map.of("projectStats", RESULTS),
        HttpServletResponse.SC_OK);
  }

  private List<Map<String, Object>> getProjectStats(String userType, String userId) throws SQLException {
    System.out.println("\n------------ DashboardStatsServlet | getProjectStats ------------");
    System.out.println("User ID: " + userId);
    System.out.println("User Type: " + userType);

    String sqlStatement = getProjectStatsSQL(userType);
    System.out.println("SQL Statement: " + sqlStatement);
    SQL_PARAMS.clear();
    if (!"admin".equals(userType)) {
      SQL_PARAMS.add(new Object[] { Integer.parseInt(userId) });
    }
    List<Map<String, Object>> results = DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, SQL_PARAMS);
    System.out.println("Project stats: " + results);
    return results;
  }

  private String getProjectStatsSQL(String userType) throws SQLException {
    switch (userType) {
      case "client":
        return "SELECT state, COUNT(*) as count FROM Projects WHERE clientId = ? GROUP BY state";
      case "projectLead":
        return "SELECT state, COUNT(*) as count FROM Projects WHERE leadId = ? GROUP BY state";
      case "admin":
        return "SELECT state, COUNT(*) as count FROM Projects GROUP BY state";
      case "validator":
        return "SELECT p.state, COUNT(*) as count FROM Projects p JOIN ProjectValidators pv ON p.projectId = pv.projectId WHERE pv.validatorId = ? GROUP BY p.state";
      default:
        throw new SQLException("Invalid user type");
    }
  }

  private List<Map<String, Object>> getRecentProjects(String userType, String userId) throws SQLException {
    System.out.println("\n------------ DashboardStatsServlet | getRecentProjects ------------");

    String sqlStatement = getRecentProjectsSQL(userType);
    SQL_PARAMS.clear();
    if (!"admin".equals(userType)) {
      SQL_PARAMS.add(new Object[] { Integer.parseInt(userId) });
    }

    List<Map<String, Object>> results = DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, SQL_PARAMS);
    System.out.println("Recent projects: " + results);
    return results;
  }

  private String getRecentProjectsSQL(String userType) throws SQLException {
    switch (userType) {
      case "client":
        return "SELECT * FROM Projects WHERE clientId = ? ORDER BY projectId DESC LIMIT 5";
      case "projectLead":
        return "SELECT * FROM Projects WHERE leadId = ? ORDER BY projectId DESC LIMIT 5";
      case "admin":
        return "SELECT * FROM Projects ORDER BY projectId DESC LIMIT 5";
      default:
        throw new SQLException("Invalid user type");
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ DashboardStatsServlet | doPost ------------");

    // int userId = 0;
    // try {
    // requestBody = RequestProtocol.parseRequest(request);
    // System.out.println("Request body: " + requestBody);
    // // Simulate requestBody

    // sqlStatements = new String[] {
    // "INSERT INTO Users (email, passwordHash, name, role, status) VALUES (?, ?, ?,
    // 'Validator', 'inactive')",
    // "UPDATE Users SET role = ? WHERE email = ?",
    // "UPDATE Users SET name = ? WHERE email = ?",
    // "SELECT userId FROM Users WHERE email = ?"

    // };

    // sqlParams.clear();

    // sqlParams.add(new Object[] { "Test@test.com",
    // "testPassword",
    // "Test User" });

    // sqlParams.add(new Object[] { "Admin", "Test@test.com" });

    // sqlParams.add(new Object[] { "Edited Test User", "Test@test.com" });

    // sqlParams.add(new Object[] { "Test@test.com" });

    // results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
    // if (results.size() > 0) {
    // System.out.println("After rs.next()");
    // userId = (int) results.get(0).get("userId");
    // System.out.println("User ID: " + userId);
    // }

    // sqlStatements = new String[] {
    // "DELETE FROM Users WHERE userId = ?"
    // };
    // sqlParams = new ArrayList<>();
    // sqlParams.add(new Object[] { userId });
    // DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

    // System.out.println("Test case done successfully");

    // ResponseProtocol.sendSuccess(request, response, this, "Test case excuted
    // successfully",
    // Map.of("userId", userId),
    // HttpServletResponse.SC_CREATED);

    // } catch (Exception e) {
    // System.out.println("Error creating validator: " + e.getMessage());

    // ResponseProtocol.sendError(request, response, this, "Error creating
    // validator", e.getMessage(),
    // HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    // }
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ DashboardStatsServlet | doPut ------------");
    // queryParams = RequestProtocol.parseQueryParams(request);
    // System.out.println("Query params: " + queryParams);

    // if (queryParams.size() > 0) {
    // ResponseProtocol.sendSuccess(request, response, this, "Query params is not
    // empty",
    // Map.of("queryParams", queryParams),
    // HttpServletResponse.SC_OK);
    // return;
    // }

    // ResponseProtocol.sendError(request, response, this, "Query params is empty",
    // Map.of("queryParams", new HashMap<>()),
    // HttpServletResponse.SC_BAD_REQUEST);
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("\n------------ DashboardStatsServlet | doDelete ------------");
    // pathParams = RequestProtocol.parsePathParams(request);
    // System.out.println("Path params: " + pathParams);
  }
}