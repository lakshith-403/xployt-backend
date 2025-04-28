package com.xployt.controller.common;

// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
import com.xployt.service.common.ProjectTeamAssignmentService;
import com.xployt.model.GenericResponse;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/api/project/team/assign/*")
public class ProjectTeamAssignmentServlet extends HttpServlet {
    private ProjectTeamAssignmentService teamAssignmentService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        teamAssignmentService = new ProjectTeamAssignmentService();
    }

    /**
     * @pathParam  api/project/team/assign/requiredRole/projectId/userId
     * returns PublicUser of requiredRole paired with userId
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("ProjectTeamAssignmentServlet: Getting assigned users");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 3) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path parameters");
            return;
        }
        String role = pathParts[1];
        String projectId = pathParts[2];
        String userId = pathParts[3];
        logger.info("Role:" + role + " ProjectId:" + projectId + " UserId:" + userId);
        GenericResponse user;
        try {
            user = teamAssignmentService.getAssignedUser(projectId, userId, role);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project team");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(user));
    }

    // @Override
    // protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    //         throws ServletException, IOException {
    //     logger.info("Assigning validators to project");
    //     response.setContentType("application/json");
    //     response.setCharacterEncoding("UTF-8");

    //     try {
    //         // Read request body using BufferedReader
    //         StringBuilder buffer = new StringBuilder();
    //         BufferedReader reader = request.getReader();
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             buffer.append(line);
    //         }
    //         String requestBody = buffer.toString();

    //         if (requestBody.isEmpty()) {
    //             logger.warning("Empty request body received");
    //             sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Request body is required");
    //             return;
    //         }

    //         // Parse JSON data
    //         JsonObject jsonData = JsonParser.parseString(requestBody).getAsJsonObject();
            
    //         // Validate required fields
    //         if (!jsonData.has("projectId") || !jsonData.has("validatorCount")) {
    //             logger.warning("Missing required fields in request");
    //             sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "projectId and validatorCount are required");
    //             return;
    //         }

    //         // Parse and validate projectId
    //         String projectId = jsonData.get("projectId").getAsString();
    //         if (projectId == null || projectId.trim().isEmpty()) {
    //             logger.warning("Invalid projectId received");
    //             sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid projectId");
    //             return;
    //         }

    //         // Parse and validate validatorCount
    //         int validatorCount;
    //         try {
    //             validatorCount = jsonData.get("validatorCount").getAsInt();
    //             if (validatorCount <= 0) {
    //                 logger.warning("Invalid validatorCount: " + validatorCount);
    //                 sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "validatorCount must be greater than 0");
    //                 return;
    //             }
    //         } catch (NumberFormatException e) {
    //             logger.warning("Invalid validatorCount format");
    //             sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "validatorCount must be a valid number");
    //             return;
    //         }

    //         // Process the request
    //         GenericResponse result = teamAssignmentService.assignValidators(projectId, validatorCount);
    //         response.getWriter().write(JsonUtil.toJson(result));
            
    //     } catch (Exception e) {
    //         logger.severe("Error assigning validators: " + e.getMessage());
    //         sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
    //     }
    // }

    // private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
    //     GenericResponse errorResponse = new GenericResponse(null, false, message, null);
    //     response.setStatus(statusCode);
    //     response.getWriter().write(JsonUtil.toJson(errorResponse));
    // }


}