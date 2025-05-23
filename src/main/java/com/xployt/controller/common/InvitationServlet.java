package com.xployt.controller.common;

import com.xployt.model.GenericResponse;
import com.xployt.model.Invitation;
import com.xployt.service.common.InvitationService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.xployt.util.CustomLogger;

@WebServlet("/api/invitations/project/*")
public class InvitationServlet extends HttpServlet {
    private InvitationService invitationService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        invitationService = new InvitationService();
    }

    private Map<String, Object> parseRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String requestBody = sb.toString();
        if (requestBody.isEmpty()) {
            throw new IOException("Request body is empty");
        }
        Gson gson = new Gson();
        Map<String, Object> jsonMap = gson.fromJson(requestBody, Map.class);
        if (jsonMap == null || jsonMap.isEmpty()) {
            throw new IOException("Invalid JSON format or empty JSON");
        }
        return jsonMap;
    }

    // Handles GET requests to fetch project invitations for a specific project (State: Pending).
    // Returns a list of invitations.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Servlet: Fetching project invitations");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path info");
            return;
        }
        String projectId = pathInfo.substring(1);

        GenericResponse ProjectInvitations;

        try {
            logger.info("Trying to fetch hacker invitations for project: " + projectId);
            ProjectInvitations = invitationService.fetchProjectInvitations(projectId);
            logger.info("project invitations: " + ProjectInvitations);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project invitations");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = JsonUtil.toJson(ProjectInvitations);
        logger.info("response: " + jsonResponse);
        response.getWriter().write(jsonResponse);

    }

    // Handles POST requests to create a new invitation.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Creating Invitation");

        Map<String, Object> jsonObject;
        try {
            jsonObject = parseRequestBody(request);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        String hackerId = jsonObject.get("hackerId") != null
                ? String.valueOf(jsonObject.get("hackerId")).replace(".0", "")
                : null;
        String projectId = jsonObject.get("projectId") != null
                ? String.valueOf(jsonObject.get("projectId")).replace(".0", "")
                : null;

        if (hackerId == null || projectId == null) {
            logger.severe("Missing parameters: hackerId or projectId is null");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        Invitation invitation = new Invitation();
        invitation.setHackerId(hackerId);
        invitation.setProjectId(projectId);

        GenericResponse HackerInvitation;

        try {
            HackerInvitation = invitationService.createInvitation(invitation);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating invitation");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(HackerInvitation));
    }
}
