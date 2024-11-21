package com.xployt.controller.lead;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.xployt.service.lead.ProjectService;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;

@WebServlet("/api/lead/project/*")
public class ProjectConfigServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();
  private static final Logger logger = CustomLogger.getLogger();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    logger.info("ProjectConfigServlet doGet method called");
    projectService.getProjectInfo(request, response);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
  }
}