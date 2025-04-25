package com.xployt.controller.client;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// import javax.servlet.ServletException;
// import java.io.IOException;
import com.xployt.service.client.ProjectService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseActionUtils;
import com.xployt.util.RequestProtocol;
import com.xployt.util.ResponseProtocol;
// import java.util.Map;

/**
 * Used to create a project request.
 * Populates the project table with initial project data
 * A project lead is assigned to the project
 */
@WebServlet("/api/client/project/request/*")
public class ProjectRequestServlet extends HttpServlet {

    private final ProjectService projectService = new ProjectService();
    private static final Logger logger = CustomLogger.getLogger();
    private ArrayList<String> pathParams;
    private static List<Object[]> sqlParams = new ArrayList<>();
    private static List<Map<String, Object>> results = new ArrayList<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        logger.info("ProjectRequestServlet doPost method called");
        try {
            if (!RequestProtocol.authorizeRequest(request, response, new String[] { "Client" })) {
                return;
            }
            projectService.createProject(request, response);
            // if (projectId == -1) {
            // ResponseProtocol.sendError(request, response, this, "Error creating project",
            // Map.of("error", "Error creating project"),
            // HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            // return;
            // }
            // ResponseProtocol.sendSuccess(request, response, this, "Project created
            // successfully",
            // Map.of("projectId", projectId),
            // HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.severe("Exception in doPost: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("\n------------ ProjectRequestServlet | doGet ------------");

        try {
            pathParams = RequestProtocol.parsePathParams(request);
            System.out.println("Path params: " + pathParams);

            String[] sqlStatements = {
                    "SELECT * FROM Projects WHERE clientId = ? AND (state = 'Pending' OR state = 'Unconfigured')"
            };
            sqlParams.clear();
            sqlParams.add(new Object[]{pathParams.get(0)});

            results = DatabaseActionUtils.executeSQL(sqlStatements, sqlParams);

            if (!results.isEmpty()) {
              System.out.println("Application Data: " + results);
                ResponseProtocol.sendSuccess(request, response, this, "Application Data fetched successfully",
                        Map.of("applicationData", results),
                        HttpServletResponse.SC_OK);
            }

        } catch (Exception e) {
            System.out.println("Error parsing request: " + e.getMessage());
            ResponseProtocol.sendError(request, response, this, "Error: Error Message", e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }
}