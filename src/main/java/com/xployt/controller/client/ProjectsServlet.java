package com.xployt.controller.client;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.service.client.ProjectsService;
import com.xployt.util.CustomLogger;

@WebServlet("/api/client/projects/*")
public class ProjectsServlet extends HttpServlet {

  private ProjectsService projectsService;
  private static final Logger logger = CustomLogger.getLogger();

  @Override
  public void init() throws ServletException {
    projectsService = new ProjectsService();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    logger.info("Fetching projects for Client");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    projectsService.fetchProjects(request, response);
  }

}
