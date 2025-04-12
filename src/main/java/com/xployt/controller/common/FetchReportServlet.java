package com.xployt.controller.common;

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
// import com.xployt.model.User;

/*
 * This is a template servlet for creating new servlets
 * It is used to be used to setup a new Controller
 * Try out theese ednpoints to se how it works
 */
@WebServlet("/api/reports/*")
public class FetchReportServlet extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  /**
   * Get a resource - Add a detailed entry
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ FetchReportServlet | doGet ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);

    if (pathParams.size() >= 4) {
      String userRole = pathParams.get(0);
      String projectId = pathParams.get(1);
      String status = pathParams.get(2);
      String userId = pathParams.get(3);

      System.out.println("User Role: " + userRole);
      System.out.println("Project ID: " + projectId);
      System.out.println("User ID: " + userId);
      try {
        List<Map<String, Object>> reports = new ArrayList<>();
        
        if ("ProjectLead".equals(userRole) || "Client".equals(userRole)) {
          sqlStatements = new String[] {
            "SELECT r.reportId, r.hackerId, r.severity, r.vulnerabilityType, r.title, r.createdAt " +
            "FROM BugReports r " +
            "WHERE r.projectId = ? AND r.status = ? " +
            "ORDER BY r.createdAt DESC"
          };
          sqlParams.clear();
          sqlParams.add(new Object[] { projectId, status });
        } else if ("Hacker".equals(userRole)) {
          sqlStatements = new String[] {
            "SELECT r.reportId, r.severity, r.vulnerabilityType, r.title, r.createdAt " +
            "FROM BugReports r " +
            "WHERE r.projectId = ? AND r.hackerId = ? AND r.status = ? " +
            "ORDER BY r.reportId"
          };
          
          sqlParams.clear();
          sqlParams.add(new Object[] { projectId, userId, status });
        } else if ("Validator".equals(userRole)) {
          sqlStatements = new String[] {
            "SELECT r.reportId, r.severity, r.vulnerabilityType, r.title, r.createdAt " +
            "FROM BugReports r " +
            "JOIN ProjectHackers ph ON r.projectId = ph.projectId AND r.hackerId = ph.hackerId " +
            "WHERE r.projectId = ? AND ph.assignedValidatorId = ? AND r.status = ? " +
            "ORDER BY r.reportId"
          };
          
          sqlParams.clear();
          sqlParams.add(new Object[] { projectId, userId, status });
        }
        
        reports = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
        
        ResponseProtocol.sendSuccess(request, response, this, "Reports fetched successfully",
            Map.of("reports", reports),
            HttpServletResponse.SC_OK);
      } catch (Exception e) {
        System.out.println("Error fetching reports: " + e.getMessage());
        ResponseProtocol.sendError(request, response, this, "Error fetching reports", 
            e.getMessage(),
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      
      return;
    }

    ResponseProtocol.sendError(request, response, this, "Invalid path parameters. Expected userRole and projectId",
        Map.of("pathParams", pathParams),
        HttpServletResponse.SC_BAD_REQUEST);

  }

}