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
import java.io.IOException;
import java.util.logging.Logger;
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Servlet: Fetching project invitations");
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

        String jsonResponse = JsonUtil.toJson(HackerInvitations);
        logger.info("response: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Creating Invitation");

        String hackerId = request.getParameter("hackerId");
        String projectId = request.getParameter("projectId");

        if (hackerId == null || projectId == null ) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        Invitation invitation = new Invitation();
        invitation.setHackerId(hackerId);
        invitation.setProjectId(projectId);

        try {
            invitationService.createInvitation(invitation);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating invitation");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(invitation));
    }
}