package com.xployt.controller.client;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import com.xployt.service.client.ProjectService;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;

@WebServlet("/api/client/project/request")
public class ProjectConfigServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();
  private static final Logger logger = CustomLogger.getLogger();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    logger.info("ProjectConfigServlet doPost method called");
    projectService.createProject(request, response);
  }
}