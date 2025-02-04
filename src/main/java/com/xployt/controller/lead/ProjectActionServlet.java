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

@WebServlet("/api/lead/initiate/project/*")
public class ProjectActionServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();
  private static final Logger logger = CustomLogger.getLogger();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      logger.warning("Lead ProjectActionServlet: Project ID not provided");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
      return;
    }

    String[] pathParts = pathInfo.split("/");
    if (pathParts.length < 3) {
      logger.warning("Lead ProjectActionServlet: Invalid URL format");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
      return;
    }

    String action = pathParts[1];
    String projectId = pathParts[2];

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    switch (action) {
      case "accept":
        projectService.acceptProject(projectId, response);
        break;
      case "reject":
        projectService.rejectProject(projectId, response);
        break;
      default:
        logger.warning("Lead ProjectActionServlet: Unknown action");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown action");
    }
  }
}