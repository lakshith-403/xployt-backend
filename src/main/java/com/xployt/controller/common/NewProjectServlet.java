package com.xployt.controller.common;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
// import java.util.HashMap;
import java.sql.SQLException;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.RequestManager;

/*
 * Fetch all the projects relevent to each user
 */
@WebServlet("/api/new-project/*")
public class NewProjectServlet extends HttpServlet {

  private static final List<Object[]> sqlParams = new ArrayList<>();
  private static final List<Map<String, Object>> results = new ArrayList<>();
  private static final List<String> pathParams = new ArrayList<>();
  // private static final Map<String, Object> requestBody = new HashMap<>();
  // private static final Map<String, Object> queryParams = new HashMap<>();
  private static String[] sqlStatements = {};

  /**
   * Fetch all the projects relevent to each user
   * 
   * @param request  the HttpServletRequest object
   * @param response the HttpServletResponse object
   * @throws IOException if an input or output error is detected
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n------------ NewProjectServlet | doGet ------------");
    try {
      pathParams.clear();
      pathParams.addAll(RequestProtocol.parsePathParams(request));

      if (!RequestProtocol.authenticateRequest(request, response)) {
        return;
      }

      if (!pathParams.isEmpty()) {
        String userType = pathParams.get(0);
        String userId = (userType.equalsIgnoreCase("admin")) ? null : (pathParams.size() > 1 ? pathParams.get(1) : null);

        sqlStatements = new String[] { getSQLForUserType(userType) };
        sqlParams.clear();
        if (userId != null) {
          sqlParams.add(new Object[] { Integer.parseInt(userId) });
        }
        results.clear();
        results.addAll(DatabaseActionUtils.executeSQL(sqlStatements, sqlParams));

        if (!results.isEmpty()) {
          List<Map<String, Object>> filteredResults = RequestManager.filterObjectsByFields(results,
              List.of("projectId", "title", "state", "clientId",
                  "leadId", "validatorId", "pendingReports"));
          ResponseProtocol.sendSuccess(request, response, this, "Projects fetched successfully",
              Map.of("projects", filteredResults), HttpServletResponse.SC_OK);
        } else {
          ResponseProtocol.sendSuccess(request, response, this, "No projects found",
              Map.of("projects", new ArrayList<>()), HttpServletResponse.SC_OK);
        }
      } else {
        ResponseProtocol.sendError(request, response, this, "Path params are empty",
            Map.of("pathParams", new ArrayList<>()), HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Error: " + e.getMessage(),
          Map.of("error", e.getMessage()), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String getSQLForUserType(String userType) throws SQLException {
    switch (userType) {
      case "Hacker":
      case "hacker":
        return "SELECT p.* FROM Projects p JOIN ProjectHackers ph ON p.projectId = ph.projectId WHERE ph.hackerId = ?";
      case "client":
      case "Client":
        return "SELECT * FROM Projects WHERE clientId = ?";
      case "lead":
      case "ProjectLead":
        return "SELECT * FROM Projects WHERE leadId = ?";
      case "admin":
      case "Admin":
        return "SELECT * FROM Projects";
      case "validator":
      case "Validator":
        return "SELECT DISTINCT p.* FROM Projects p JOIN ProjectHackers ph ON p.projectId = ph.projectId WHERE ph.assignedValidatorId = ?";
      default:
        throw new SQLException("Invalid user type");
    }
  }
}