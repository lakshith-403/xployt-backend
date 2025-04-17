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
@WebServlet("/api/fetch-all-reports/*")
public class FetchAllReports extends HttpServlet {

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

    System.out.println("\n------------ FetchAllReports | doGet ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);

    if (pathParams.size() >= 2) {
      String userRole = pathParams.get(0);
      String userId = pathParams.get(1);

      System.out.println("User Role: " + userRole);
      System.out.println("User ID: " + userId);

      try {
        List<Map<String, Object>> reports = new ArrayList<>();
        
        switch (userRole) {
          case "ProjectLead":
            sqlStatements = new String[] {
              "SELECT r.reportId, r.hackerId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, p.projectId, p.title as projectTitle " +
              "FROM BugReports r " +
              "JOIN Projects p ON r.projectId = p.projectId " +
              "WHERE p.leadId = ? " +
              "ORDER BY r.createdAt DESC"
            };
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            break;

          case "Client":
            sqlStatements = new String[] {
              "SELECT r.reportId, r.hackerId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, p.projectId, p.title as projectTitle " +
              "FROM BugReports r " +
              "JOIN Projects p ON r.projectId = p.projectId " +
              "WHERE p.clientId = ? " +
              "ORDER BY r.createdAt DESC"
            };
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            break;

          case "Hacker":
            sqlStatements = new String[] {
              "SELECT r.reportId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, p.projectId, p.title as projectTitle " +
              "FROM BugReports r " +
              "JOIN Projects p ON r.projectId = p.projectId " +
              "WHERE r.hackerId = ? " +
              "ORDER BY r.createdAt DESC"
            };
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            break;

          case "Validator":
            sqlStatements = new String[] {
              "SELECT r.reportId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, p.projectId, p.title as projectTitle " +
              "FROM BugReports r " +
              "JOIN Projects p ON r.projectId = p.projectId " +
              "JOIN ProjectHackers ph ON r.projectId = ph.projectId AND r.hackerId = ph.hackerId " +
              "WHERE ph.assignedValidatorId = ? " +
              "ORDER BY r.createdAt DESC"
            };
            sqlParams.clear();
            sqlParams.add(new Object[] { userId });
            break;

          default:
            ResponseProtocol.sendError(request, response, this, "Invalid user role",
                Map.of("userRole", userRole),
                HttpServletResponse.SC_BAD_REQUEST);
            return;
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

    ResponseProtocol.sendError(request, response, this, "Invalid path parameters. Expected userRole and userId",
        Map.of("pathParams", pathParams),
        HttpServletResponse.SC_BAD_REQUEST);
  }
}