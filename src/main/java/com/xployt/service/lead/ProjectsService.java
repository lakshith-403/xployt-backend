package com.xployt.service.lead;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.dao.lead.ProjectsDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectBriefLead;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ProjectsService {

  public void fetchProjects(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final List<String> STATUS_FILTER = Arrays.asList("Completed", "Rejected");
    final Logger logger = CustomLogger.getLogger();

    logger.info("Lead ProjectsService: Inside fetchProjects");
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.isEmpty()) {
      logger.severe("Lead ProjectsService: User ID not provided hehe hoo");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided hehe hoo");
      return;
    }
    String userId = pathInfo.substring(1); // Get userId from URL
    logger.info("Lead ProjectsService: User ID: " + userId);
    ProjectsDAO projectsDAO = new ProjectsDAO();
    List<ProjectBriefLead> allProjects = projectsDAO.getAllProjects(userId);

    List<ProjectBriefLead> filteredProjects = new ArrayList<>();
    List<ProjectBriefLead> remainingProjects = new ArrayList<>();

    for (ProjectBriefLead project : allProjects) {
      if (!STATUS_FILTER.contains(project.getState())) {
        filteredProjects.add(project);
      } else {
        remainingProjects.add(project);
      }
    }

    logger.info("Lead ProjectsService: Filtered projects: " + filteredProjects.size());
    List<List<ProjectBriefLead>> result = new ArrayList<>();
    result.add(filteredProjects);
    result.add(remainingProjects);

    try {
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, true, null, null)));
    } catch (IOException e) {
      logger.severe("Lead ProjectsService: Error writing response: " + e.getMessage());
      response.getWriter().write(JsonUtil.toJson(new GenericResponse(result, false, null, null)));
    }
  }
}
