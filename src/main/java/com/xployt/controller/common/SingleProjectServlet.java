package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/single-project/*")
public class SingleProjectServlet extends HttpServlet {

  private static final List<Object[]> sqlParams = new ArrayList<>();
  private static final List<Map<String, Object>> results = new ArrayList<>();
  private static final Map<String, Object> queryParams = new HashMap<>();

  /**
   * Get the project details for the given project id and role
   * 
   * gets the following details:
   * - project id
   * - project name
   * - project description
   * - project technical stack
   * - project start date
   * - project state
   * - project initial funding
   * @param request
   * @param response
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ SingleProjectServlet | doGet ------------");
    if (!RequestProtocol.authenticateRequest(request, response)) {
      return;
    }

    try {
      List<String> pathParams = RequestProtocol.parsePathParams(request);
      String projectId = pathParams.size() > 0 ? pathParams.get(0) : null;
      queryParams.putAll(RequestProtocol.parseQueryParams(request));
      System.out.println("queryParams: " + queryParams);
      String role = (String) queryParams.get("role");
      System.out.println("projectId: " + projectId);
      System.out.println("role: " + role);

      if (projectId != null && role != null) {
        String sqlStatement = getSQLForRole(role);
        sqlParams.clear();
        sqlParams.add(new Object[] { Integer.parseInt(projectId) });

        results.clear();
        results.addAll(DatabaseActionUtils.executeSQL(new String[] { sqlStatement }, sqlParams));

        if (!results.isEmpty()) {
          ResponseProtocol.sendSuccess(request, response, this, "Project fetched successfully",
              Map.of("project", results.get(0)), HttpServletResponse.SC_OK);
        } else {
          ResponseProtocol.sendSuccess(request, response, this, "No project found",
              Map.of("project", new ArrayList<>()), HttpServletResponse.SC_NOT_FOUND);
        }
      } else {
        ResponseProtocol.sendError(request, response, this, "Invalid path params or role",
            Map.of("pathParams", pathParams, "role", role), HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error: " + e.getMessage(),
          Map.of("error", e.getMessage()), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getSQLForRole(String role) throws SQLException {
    switch (role) {
      case "Client":
      case "client":
        return "SELECT p.*, pc.* FROM Projects p LEFT JOIN ProjectConfigs pc ON p.projectId = pc.projectId WHERE p.projectId = ?";
      case "ProjectLead":
      case "Lead":
      case "lead":
        return "SELECT p.*, pc.* FROM Projects p LEFT JOIN ProjectConfigs pc ON p.projectId = pc.projectId WHERE p.projectId = ?";
      case "Admin":
      case "admin":
        return "SELECT p.*, pc.* FROM Projects p LEFT JOIN ProjectConfigs pc ON p.projectId = pc.projectId WHERE p.projectId = ?";
      case "Validator":
      case "validator":
        return "SELECT p.*, pc.* FROM Projects p LEFT JOIN ProjectConfigs pc ON p.projectId = pc.projectId WHERE p.projectId = ?";
      case "Hacker":
      case "hacker":
        return "SELECT * FROM Projects WHERE projectId = ?";
      default:
        throw new SQLException("Invalid role");
    }
  }
}
