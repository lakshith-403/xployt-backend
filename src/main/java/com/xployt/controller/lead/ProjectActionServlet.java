package com.xployt.controller.lead;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import com.xployt.service.lead.ProjectService;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;
import java.util.ArrayList;
import java.util.List;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.ResponseProtocol;
import com.xployt.util.RequestProtocol;
import java.util.Map;

@WebServlet("/api/lead/initiate/project/*")
public class ProjectActionServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();
  private static final Logger logger = CustomLogger.getLogger();

  /**
   * ProjectActionServlet handles actions related to project management by project leads.
   * This servlet processes requests for accepting, rejecting, or proceeding with projects.
   * It updates the project state and maintains project lead statistics accordingly.
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      logger.warning("Lead ProjectActionServlet: Project ID not provided");
      ResponseProtocol.sendError(request, response, this, "Project ID not provided",
          "Project ID not provided", HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String[] pathParts = pathInfo.split("/");
    if (pathParts.length < 3) {
      logger.warning("Lead ProjectActionServlet: Invalid URL format");
      ResponseProtocol.sendError(request, response, this, "Invalid URL format",
          "Invalid URL format", HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String action = pathParts[1];
    String projectId = pathParts[2];
    Map<String, Object> requestBody = RequestProtocol.parseRequest(request);
    int projectLeadId = ((Number) requestBody.get("projectLeadId")).intValue();

    try {
      switch (action) {
        case "accept":
          projectService.acceptProject(projectId, response, request);
          break;
        case "reject":
          projectService.rejectProject(projectId, response, request);
          
          String[] rejectSql = {
            "UPDATE ProjectLeadInfo SET rejectedProjectCount = rejectedProjectCount + 1 WHERE projectLeadId = ?",
            "UPDATE ProjectLeadInfo SET activeProjectCount = activeProjectCount - 1 WHERE projectLeadId = ?"
          };
          List<Object[]> rejectParams = new ArrayList<>();
          rejectParams.add(new Object[] { projectLeadId });
          rejectParams.add(new Object[] { projectLeadId });
          DatabaseActionUtils.executeSQL(rejectSql, rejectParams);
          break;
        case "proceed":
          String[] proceedSql = {
            "UPDATE Projects SET state = 'Active' WHERE projectId = ?",
          };
          List<Object[]> proceedParams = new ArrayList<>();
          proceedParams.add(new Object[] { projectId });
          DatabaseActionUtils.executeSQL(proceedSql, proceedParams);
          
          ResponseProtocol.sendSuccess(request, response, this, "Project updated to Active",
              "Project updated to Active", HttpServletResponse.SC_OK);
          break;
        default:
          logger.warning("Lead ProjectActionServlet: Unknown action");
          ResponseProtocol.sendError(request, response, this, "Unknown action",
              "Unknown action", HttpServletResponse.SC_BAD_REQUEST);
      }
    } catch (Exception e) {
      logger.severe("Lead ProjectActionServlet: Error processing request");
      e.printStackTrace();
      ResponseProtocol.sendError(request, response, this, "Error processing request",
          "Error processing request", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }
}