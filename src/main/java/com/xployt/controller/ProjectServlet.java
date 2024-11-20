package com.xployt.controller;

import com.xployt.model.GenericResponse;
import com.xployt.service.ProjectService;
import com.xployt.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;
import com.xployt.util.CustomLogger;

@WebServlet("/api/validator/projects/*") // Matches /api/validator/projects/{userId}
public class ProjectServlet extends HttpServlet {
    private ProjectService projectService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectService = new ProjectService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Fetching projects for user test Hehe");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        String userId = pathInfo.substring(1); // Get userId from URL
        GenericResponse projects;
        try {
            projects = projectService.fetchProjects(userId);
            logger.info("project: " + projects);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching projects");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Use the custom JsonUtil to convert projects to JSON
        String jsonResponse = JsonUtil.toJson(projects);
        logger.info("jsonResponse is: " + jsonResponse);
        response.getWriter().write(jsonResponse);
        // response.getWriter().write("");
    }
}
