package com.xployt.controller.hacker;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;

@WebServlet("/api/hacker/reportStatus/*")
public class HackerReportStatusServlet extends HttpServlet {

  private static ArrayList<String> pathParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ HackerReportStatusServlet | doGet ------------");

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

      // Get project hacker information
      String[] sqlStatements = new String[] {
        "SELECT ph.*, u.name, u.email " +
        "FROM projecthackers ph " +
        "JOIN users u ON ph.hackerId = u.userId " +
        "WHERE ph.projectId = ? AND ph.hackerId = ?"
      };
      List<Object[]> sqlParams = new ArrayList<>();
      sqlParams.add(new Object[] { projectId, hackerId });

      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

      if (results.isEmpty()) {
        ResponseProtocol.sendError(request, response, this,
            "No project hacker information found",
            "No record found for the given project and hacker IDs",
            HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      // Success response
      ResponseProtocol.sendSuccess(request, response, this,
          "Project hacker information retrieved successfully",
          Map.of("hackerInfo", results.get(0)),
          HttpServletResponse.SC_OK);

    } catch (Exception e) {
      System.out.println("Error getting project hacker information: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this,
          "Error getting project hacker information", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
