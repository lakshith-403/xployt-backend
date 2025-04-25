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

@WebServlet("/api/lead/stats/*")
public class StatsServlet extends HttpServlet {

  private static List<Object[]> sqlParams = new ArrayList<>();
  private static List<Map<String, Object>> results = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  private static ArrayList<String> pathParams = new ArrayList<>();
  private static Map<String, Object> queryParams = new HashMap<>();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    System.out.println("\n------------ StatsServlet | doGet ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);

      if (pathParams.size() == 2) {
        int projectId = Integer.parseInt(pathParams.get(0));
        String type = pathParams.get(1).toLowerCase();

        String procedureCall = null;

        switch (type) {
          case "hacker":
            procedureCall = "{CALL GetHackerReportStats(?)}";
            break;
          case "validator":
            procedureCall = "{CALL GetValidatorReportStats(?)}";
            break;
          case "vuln":
            procedureCall = "{CALL GetValidatedVulnerabilityCounts(?)}";
            break;
          case "saved": {
            // --- 1. Fetch feedback from LeadReport ---
            String[] feedbackSQL = new String[] {
              "SELECT vulnerabilityType, suggestions FROM LeadReport WHERE projectId = ?"
            };
            List<Object[]> feedbackParams = new ArrayList<>();
            feedbackParams.add(new Object[] { projectId });

            List<Map<String, Object>> feedbackData = DatabaseActionUtils.executeSQL(feedbackSQL, feedbackParams);

            // --- 2. Fetch summary from LeadSummary ---
            String[] summarySQL = new String[] {
              "SELECT summary FROM LeadSummary WHERE projectId = ?"
            };
            List<Object[]> summaryParams = new ArrayList<>();
            summaryParams.add(new Object[] { projectId });

            List<Map<String, Object>> summaryResult = DatabaseActionUtils.executeSQL(summarySQL, summaryParams);

            String summaryText = "";
            if (!summaryResult.isEmpty() && summaryResult.get(0).get("summary") != null) {
              summaryText = summaryResult.get(0).get("summary").toString();
            }

            // --- 3. Send Combined Response ---
            ResponseProtocol.sendSuccess(request, response, this,
                "Loaded saved report",
                Map.of(
                  "feedback", feedbackData,
                  "summary", summaryText
                ),
                HttpServletResponse.SC_OK);
            return;
          }

          default:
            ResponseProtocol.sendError(request, response, this, "Invalid report type: " + type,
                Map.of("error", "Supported types are: project, validator"),
                HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Object[] procedureParams = new Object[] { projectId };
        results = DatabaseActionUtils.callProcedure(procedureCall, procedureParams);

        ResponseProtocol.sendSuccess(request, response, this,
            "Report stats retrieved",
            Map.of("reportStats", results),
            HttpServletResponse.SC_OK);

      } else {
        ResponseProtocol.sendError(request, response, this, "Missing or too many path parameters",
            Map.of("expectedFormat", "/projectId/type"),
            HttpServletResponse.SC_BAD_REQUEST);
      }

    } catch (Exception e) {
      System.out.println("Error: " + e.getMessage());
      ResponseProtocol.sendError(request, response, this, "Exception occurred: " + e.getMessage(),
          Map.of("error", e.getMessage()),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ StatsServlet | doPost ------------");

    try {
      pathParams = RequestProtocol.parsePathParams(request);
      int projectId = Integer.parseInt(pathParams.get(0));
      
      requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      String summary = requestBody.get("summary").toString();
      @SuppressWarnings("unchecked")
      Map<String, Object> feedbackMap = (Map<String, Object>) requestBody.get("feedback");

      // ✅ 1. Prepare and insert/update summary using executeSQL
      String[] sqlStatements = new String[] {
        "INSERT INTO LeadSummary (projectId, summary) VALUES (?, ?) ON DUPLICATE KEY UPDATE summary = VALUES(summary)"
      };
      List<Object[]> summaryParams = new ArrayList<>();
      summaryParams.add(new Object[] { projectId, summary });

      DatabaseActionUtils.executeSQL(sqlStatements, summaryParams);

      // ✅ 2. Insert/update feedback in batch using executeBatchSQL
      String feedbackSQL = "INSERT INTO LeadReport (projectId, vulnerabilityType, suggestions) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE suggestions = VALUES(suggestions)";
      List<Object[]> feedbackParams = new ArrayList<>();

      for (Map.Entry<String, Object> entry : feedbackMap.entrySet()) {
        String vulnType = entry.getKey();
        String suggestion = entry.getValue().toString();
        feedbackParams.add(new Object[] { projectId, vulnType, suggestion });
      }

      DatabaseActionUtils.executeBatchSQL(feedbackSQL, feedbackParams);

      // ✅ Success response
      ResponseProtocol.sendSuccess(request, response, this,
          "Lead report submitted successfully",
          Map.of("status", "success"),
          HttpServletResponse.SC_CREATED);

    } catch (Exception e) {
      System.out.println("Error submitting lead report: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this,
          "Error submitting lead report", e.getMessage(),
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}
