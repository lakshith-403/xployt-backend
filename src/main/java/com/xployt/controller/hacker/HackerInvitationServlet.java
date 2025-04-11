package com.xployt.controller.hacker;

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

@WebServlet("/api/invitations/hacker/*")

public class HackerInvitationServlet extends HttpServlet {
    private InvitationService invitationService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        invitationService = new InvitationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Servlet: Fetching hacker invitations");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path info");
            return;
        }
        String userId = pathInfo.substring(1);

        GenericResponse HackerInvitations;

        try {
            logger.info("Trying to fetch hacker invitations for user: " + userId);
            HackerInvitations = invitationService.fetchHackerInvitations(userId);
            logger.info("project invitations: " + HackerInvitations);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project invitations");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(HackerInvitations));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String requestBody = sb.toString();
        Gson gson = new Gson();
        return gson.fromJson(requestBody, Map.class);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Accepting Invitation");

        Map<String, Object> jsonObject;
        logger.info("Parsing request body");
        try {
            logger.info("Parsing request body");
            jsonObject = parseRequestBody(request);
            logger.info("jsonObject: " + jsonObject);
        } catch (Exception e) {
            logger.severe("Error parsing JSON: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        String hackerId = jsonObject.get("hackerId") != null
                ? new Gson().toJson(jsonObject.get("hackerId")).replace(".0", "").replace("\"", "")
                : null;
        logger.info("hackerId: " + hackerId);
        String projectId = jsonObject.get("projectId") != null
                ? new Gson().toJson(jsonObject.get("projectId")).replace(".0", "").replace("\"", "")
                : null;
        logger.info("projectId: " + projectId);
        Boolean accepted = jsonObject.get("accept") != null
                ? new Gson().fromJson(jsonObject.get("accept").toString(), Boolean.class)
                : null;
        logger.info("accepted: " + accepted);

        if (hackerId == null || projectId == null || accepted == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        logger.info("Accepting invitation for projectId: " + projectId + ", hackerId: " + hackerId + ", accepted: " + accepted);

        Invitation invitation = new Invitation();
        invitation.setHackerId(hackerId);
        invitation.setProjectId(projectId);


        GenericResponse updatedInvitation;

        try {
            if (Boolean.TRUE.equals(accepted)) {
                invitation.setStatus("Accepted");
                updatedInvitation = invitationService.acceptInvitation(invitation);
            } else {
                invitation.setStatus("Declined");
                updatedInvitation = invitationService.rejectInvitation(invitation);
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error accepting invitation");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(updatedInvitation));
    }
}