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
}
