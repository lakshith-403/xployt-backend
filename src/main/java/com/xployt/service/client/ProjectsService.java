package com.xployt.service.client;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.dao.client.ProjectsDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectBrief;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ProjectsService {

  public void fetchProjects(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final List<String> INACTIVE_FILTER = Arrays.asList("Completed", "Rejected");
    final List<String> REQUESTS_FILTER = Arrays.asList("Pending", "Unconfigured");
    final Logger logger = CustomLogger.getLogger();

    logger.info("Client ProjectsService: Inside fetchProjects");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      logger.severe("Client ProjectsService: User ID not provided hehe hoo");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided hehe hoo");
      return;
    }
    String userId = pathInfo.substring(1); // Get userId from URL
    logger.info("Client ProjectsService: User ID: " + userId);
    ProjectsDAO projectsDAO = new ProjectsDAO();
    List<ProjectBrief> allProjects = projectsDAO.getAllProjects(userId);

    List<ProjectBrief> requestedProjects = new ArrayList<>();
    List<ProjectBrief> inactiveProjects = new ArrayList<>();
    List<ProjectBrief> activeProjects = new ArrayList<>();

    for (ProjectBrief project : allProjects) {
      if (INACTIVE_FILTER.contains(project.getState())) {
        inactiveProjects.add(project);
      } else if (REQUESTS_FILTER.contains(project.getState())) {
        requestedProjects.add(project);
      } else {
        activeProjects.add(project);
      }
    }

    logger.info("Client ProjectsService: Filtered projects: " + requestedProjects.size());
    List<List<ProjectBrief>> result = new ArrayList<>();
    result.add(activeProjects);
    result.add(requestedProjects);
    result.add(inactiveProjects);

    try {
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, true, null, null)));
    } catch (IOException e) {
      logger.severe("Client ProjectsService: Error writing response: " + e.getMessage());
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, false, null, null)));
    }
  }
}
