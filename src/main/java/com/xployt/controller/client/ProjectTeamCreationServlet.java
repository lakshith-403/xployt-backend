package com.xployt.controller.client;

import com.google.gson.Gson;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectTeamRequest;
import com.xployt.service.client.ProjectTeamCreationService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/api/client/project/team/create")
public class ProjectTeamCreationServlet extends HttpServlet {
    private ProjectTeamCreationService projectTeamCreationService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectTeamCreationService = new ProjectTeamCreationService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        logger.info("Creating project team");

        try {
            // Read request body into string
            StringBuilder buffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String requestBody = buffer.toString();

            // Parse JSON string into object
            ProjectTeamRequest teamRequest = JsonUtil.fromJson(requestBody, ProjectTeamRequest.class);

            GenericResponse result = projectTeamCreationService.createProjectTeam(teamRequest);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(result));

        } catch (Exception e) {
            logger.severe("Error creating project team: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating project team");
        }
    }
} 