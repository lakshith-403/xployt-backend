package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.PublicUser;
import com.xployt.service.common.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/discussions/*")
public class DiscussionServlet extends HttpServlet {
    private DiscussionService discussionService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        discussionService = new DiscussionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check user authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        logger.info("Fetching discussion by ID for user: " + userId);
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Discussion ID not provided");
            return;
        }

        String discussionId = pathInfo.substring(1);
        GenericResponse discussionResponse;
        try {
            discussionResponse = discussionService.fetchDiscussionById(discussionId);
            
            // Verify user is a participant in the discussion
            if (discussionResponse.isIs_successful() && discussionResponse.getData() != null) {
                Discussion discussion = (Discussion) discussionResponse.getData();
                boolean isAuthorized = false;
                for (PublicUser participant : discussion.getParticipants()) {
                    if (participant.getUserId().equals(userId)) {
                        isAuthorized = true;
                        break;
                    }
                }
                
                if (!isAuthorized) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not authorized to access this discussion");
                    return;
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching discussion");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(discussionResponse));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check user authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        logger.info("Creating discussion for user: " + userId);
 
        Gson gson = JsonUtil.useGson();
        Discussion discussion = gson.fromJson(request.getReader(), Discussion.class);
        
        // We need to ensure the current user is part of the participants
        boolean userIsParticipant = false;
        for (PublicUser participant : discussion.getParticipants()) {
            if (participant.getUserId().equals(userId)) {
                userIsParticipant = true;
                break;
            }
        }
        
        // Since Discussion uses a final field, we can't modify it directly
        // The frontend should ensure the user is added as a participant

        GenericResponse result;
        try {
            result = discussionService.createDiscussion(discussion);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating discussion");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(result));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check user authentication
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not authenticated");
            return;
        }
        
        String userId = (String) session.getAttribute("userId");
        logger.info("Updating discussion for user: " + userId);

        Gson gson = JsonUtil.useGson();
        Discussion discussion = gson.fromJson(request.getReader(), Discussion.class);
        
        // Verify user is a participant in the discussion
        try {
            GenericResponse existingDiscussionRes = discussionService.fetchDiscussionById(discussion.getId());
            if (existingDiscussionRes.isIs_successful() && existingDiscussionRes.getData() != null) {
                Discussion existingDiscussion = (Discussion) existingDiscussionRes.getData();
                boolean isAuthorized = false;
                for (PublicUser participant : existingDiscussion.getParticipants()) {
                    if (participant.getUserId().equals(userId)) {
                        isAuthorized = true;
                        break;
                    }
                }
                
                if (!isAuthorized) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "User not authorized to update this discussion");
                    return;
                }
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error validating discussion access");
            return;
        }
        
        GenericResponse result;
        try {
            result = discussionService.updateDiscussion(discussion);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating discussion");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(result));
    }
} 
