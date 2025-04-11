package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.service.common.ProjectService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/project/*")
public class ProjectServlet extends HttpServlet {
    private ProjectService projectService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectService = new ProjectService();
    }

    @Override
    // add a description on servlet, services, return objects and parameters to be passed
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Fetching project");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        String projectId = pathInfo.substring(1);
        GenericResponse project;
        try {
            project = projectService.getProjectById(projectId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching projects");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(project));
    }
}
