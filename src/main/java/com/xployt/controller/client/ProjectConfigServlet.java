package com.xployt.controller.client;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import com.xployt.service.client.ProjectService;

@WebServlet("/api/client/project/config")
public class ProjectConfigServlet extends HttpServlet {

  private final ProjectService projectService = new ProjectService();

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    projectService.createProject(request, response);
  }
}