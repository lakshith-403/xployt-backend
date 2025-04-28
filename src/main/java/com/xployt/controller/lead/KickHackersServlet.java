package com.xployt.controller.lead;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
// import java.util.HashMap;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/lead/manageHacker/*")
public class KickHackersServlet extends HttpServlet {

  // private static List<Object[]> sqlParams = new ArrayList<>();
  // private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();

  
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ KickHackersServlet | doDelete ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);
      if (pathParams.size() != 2) {
        ResponseProtocol.sendError(request, response, "Invalid path parameters", null, HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      int projectId = Integer.parseInt(pathParams.get(0));
      int hackerId = Integer.parseInt(pathParams.get(1));
      System.out.println("Project ID: " + projectId);
      System.out.println("Hacker ID: " + hackerId);
      RequestProtocol.isUserRelatedToProject(request, response, projectId);
      
      // ✅ 1. Prepare and insert/update summary using executeSQL
      String[] sqlStatements = new String[] {
        "UPDATE projecthackers SET status = 'Kicked' WHERE projectId = ? AND hackerId = ?"
      };
      List<Object[]> summaryParams = new ArrayList<>();
      summaryParams.add(new Object[] { projectId, hackerId });

      DatabaseActionUtils.executeSQL(sqlStatements, summaryParams);

      // ✅ Success response
      ResponseProtocol.sendSuccess(request, response, this,
          "Hacker deactivated successfully",
          Map.of("status", "success"),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error removing hacker: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this,
          "Error removing hacker", e.getMessage(),
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ KickHackersServlet | doGet ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);
      if (pathParams.size() != 1) {
        ResponseProtocol.sendError(request, response, "Invalid path parameters", null, HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      int projectId = Integer.parseInt(pathParams.get(0));
      System.out.println("Project ID: " + projectId);
      RequestProtocol.isUserRelatedToProject(request, response, projectId);

      // Get all project hackers
      String[] sqlStatements = new String[] {
        "SELECT ph.*, u.name, u.email " +
        "FROM projecthackers ph " +
        "JOIN users u ON ph.hackerId = u.userId " +
        "WHERE ph.projectId = ?"
      };
      List<Object[]> sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { projectId });

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      // Success response
      ResponseProtocol.sendSuccess(request, response, this,
          "Project hackers retrieved successfully",
          Map.of("hackers", results),
          HttpServletResponse.SC_OK);

    } catch (Exception e) {
      System.out.println("Error getting project hackers: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this,
          "Error getting project hackers", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
