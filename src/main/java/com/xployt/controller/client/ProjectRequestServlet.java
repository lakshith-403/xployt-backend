package com.xployt.controller.client;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// import javax.servlet.ServletException;
// import java.io.IOException;
import com.xployt.service.client.ProjectService;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;
// import com.xployt.util.ResponseProtocol;
// import java.util.Map;

/**
 * Used to create a project request.
 * Populates the project table with initial project data
 * A project lead is assigned to the project
 */
@WebServlet("/api/client/project/request")
public class ProjectRequestServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();
  private static final Logger logger = CustomLogger.getLogger();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    logger.info("ProjectRequestServlet doPost method called");
    try {
      projectService.createProject(request, response);
      // if (projectId == -1) {
      // ResponseProtocol.sendError(request, response, this, "Error creating project",
      // Map.of("error", "Error creating project"),
      // HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      // return;
      // }
      // ResponseProtocol.sendSuccess(request, response, this, "Project created
      // successfully",
      // Map.of("projectId", projectId),
      // HttpServletResponse.SC_OK);
    } catch (Exception e) {
      logger.severe("Exception in doPost: " + e.getMessage());
    }
  }
}