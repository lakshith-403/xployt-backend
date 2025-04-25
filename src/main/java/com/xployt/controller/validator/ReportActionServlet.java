package com.xployt.controller.validator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.xployt.dao.common.BlastPointsDAO;
import com.xployt.dao.common.VulnerabilityReportDAO;
import com.xployt.model.VulnerabilityReport;
import com.xployt.dao.common.NotificationDAO;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.DatabaseActionUtils;
// import com.xployt.model.User;

/*
 * This is a for managing reports from validators POV
 */
@WebServlet("/api/validator/reportAction/*")
public class ReportActionServlet extends HttpServlet {

  private static String[] sqlStatements = {};
  private static List<Object[]> sqlParams = new ArrayList<>();
  private static Map<String, Object> requestBody = new HashMap<>();
  // private static List<Map<String, Object>> results = new ArrayList<>();
  // private static ArrayList<String> pathParams = new ArrayList<>();
  // private static Map<String, Object> queryParams = new HashMap<>();

  /**
   * Updates the status of a report by validators POV
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("\n\n------------ ReportActionServlet | doPost ------------");

    try {
      requestBody = RequestProtocol.parseRequest(request);
      System.out.println("Request body: " + requestBody);

      String reportId = (String) requestBody.get("reportId");
      String actionType = (String) requestBody.get("actionType");
      String feedback = (String) requestBody.get("feedback");
      System.out.println("Report ID: " + reportId);
      System.out.println("Action type: " + actionType);
      System.out.println("Feedback: " + feedback);
      sqlStatements = new String[] {
          "UPDATE BugReports SET status = ?, feedback = ? WHERE reportId = ?"
      };

      sqlParams.clear();
      sqlParams.add(new Object[] { actionType, feedback, reportId });

      DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);
      System.out.println("Report status updated successfully with status: " + actionType);

        // Add blast points for the user based on the action taken
      BlastPointsDAO blastPointsDAO = new BlastPointsDAO();
      String blastPointsCategory = "report_submission";
      try {

        VulnerabilityReport report = new VulnerabilityReportDAO().getVulnerabilityReportById(Integer.parseInt(reportId));
        String action = "";
        switch (actionType) {
          case "More Info":
            action = "flagged_for_more_info";
            break;
          case "Rejected":
            action = "rejected";
            break;
          case "Validated":
            action = report.getSeverity().toLowerCase();
            break;
        }
        if (!action.isEmpty()) {
          blastPointsDAO.addUserBlastPoints(Integer.parseInt(report.getHackerId()), blastPointsCategory, action);
        }
        System.out.println("Blast points added for user: " + report.getHackerId() + " with action: " + action);

        //      Notification
        NotificationDAO notificationDAO = new NotificationDAO();
        String notificationMessage;
        if (actionType.equals("More Info")) {
          notificationMessage = "Bug report " + report.getReportId() + " requests more information";
        } else {
          notificationMessage = "Bug report " + report.getReportId() + " has been " + actionType;
        }
        notificationDAO.createNotification(
                report.getHackerId(),
                "Report Submission #" + report.getProjectId(),
                notificationMessage,
                "/reports/vulnerability/" + report.getProjectId() + "/" + report.getReportId()
        );

      } catch (Exception e) {
        System.out.println("Error adding blast points: " + e.getMessage());
      }

      ResponseProtocol.sendSuccess(request, response, this, "Report status updated successfully",
          Map.of("reportId", requestBody.get("reportId")),
          HttpServletResponse.SC_CREATED);


    } catch (Exception e) {
      System.out.println("Error updating report status: " + e.getMessage());

      ResponseProtocol.sendError(request, response, this, "Error updating report status", e.getMessage(),
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    }
  }
}