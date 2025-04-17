package com.xployt.controller.client;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Logger;

import com.xployt.model.GenericResponse;
import com.xployt.service.client.ProjectService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/client/project/*")
public class ProjectServlet extends HttpServlet {

    private final ProjectService projectService = new ProjectService();
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("ProjectServlet: closing project");

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
            return;
        }
        String projectId = pathInfo.substring(1);

     GenericResponse project;
        try {
            project = projectService.closeProject(Integer.parseInt(projectId));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error closing project");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(project));

    }
}
