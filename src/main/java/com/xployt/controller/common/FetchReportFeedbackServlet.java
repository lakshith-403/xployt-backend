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

import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
// import com.xployt.model.User;

/*
 * This servlet handles fetching feedback for vulnerability reports.
 * It provides an endpoint to retrieve validator feedback, update timestamp,
 * and assigned validator ID for a specific report.
 */
@WebServlet("/api/reports/feedback/*")
public class FetchReportFeedbackServlet extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  // private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  // private static Map<String, Object> queryParams = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ FetchReportFeedbackServlet | doGet ------------");
    pathParams = RequestProtocol.parsePathParams(request);
    System.out.println("Path params: " + pathParams);

    if (pathParams.size() != 1) {
      ResponseProtocol.sendError(request, response, this, "Invalid path parameters",
          Map.of("error", "Report ID is required"),
          HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String reportId = pathParams.get(0);
    try {
      sqlStatements = new String[] {
        "SELECT ph.assignedValidatorId, br.feedback, br.updatedAt " +
        "FROM BugReports br " +
        "JOIN ProjectHackers ph ON br.projectId = ph.projectId AND br.hackerId = ph.hackerId " +
        "WHERE br.reportId = ?"
      };
      sqlParams.clear();
      sqlParams.add(new Object[] { reportId });
      
      results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      
      if (results.isEmpty()) {
        ResponseProtocol.sendError(request, response, this, "Report not found",
            Map.of("error", "No feedback found for the given report ID"),
            HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      Map<String, Object> feedbackData = results.get(0);
      ResponseProtocol.sendSuccess(request, response, this, "Feedback retrieved successfully",
          Map.of(
            "assignedValidatorId", feedbackData.get("assignedValidatorId"),
            "feedback", feedbackData.get("feedback"),
            "updatedAt", feedbackData.get("updatedAt")
          ),
          HttpServletResponse.SC_OK);
    } catch (Exception e) {
      ResponseProtocol.sendError(request, response, this, "Error retrieving feedback",
          Map.of("error", e.getMessage()),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }
}