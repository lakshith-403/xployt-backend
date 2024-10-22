package com.xployt.controller;

import com.xployt.model.Project;
import com.xployt.service.ProjectService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/validator/projects/*") // Matches /api/validator/projects/{userId}
public class ProjectServlet extends HttpServlet {
    private ProjectService projectService;

    @Override
    public void init() throws ServletException {
        projectService = new ProjectService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        String userId = pathInfo.substring(1); // Get userId from URL
        List<Project> projects;

        try {
            projects = projectService.fetchProjects(userId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching projects");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Use the custom JsonUtil to convert projects to JSON
        String jsonResponse = JsonUtil.toJson(projects);
        response.getWriter().write(jsonResponse);
    }
}
