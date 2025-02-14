package com.xployt.controller.validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import com.xployt.util.CustomLogger;
import com.xployt.service.common.ProjectService;

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
        logger.info("Fetching projects for user");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided hehe hoo");
            return;
        }
        logger.info("pathInfo: " + pathInfo);
        logger.info("pathInfo substring: " + pathInfo.substring(1));

        String userId = pathInfo.substring(1); // Get userId from URL
        try {
            projectService.fetchProjects(userId, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching projects");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

    }
}
