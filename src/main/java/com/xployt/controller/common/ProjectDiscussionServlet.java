package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.service.common.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/project_discussions/*")
public class ProjectDiscussionServlet extends HttpServlet {
    private final DiscussionService discussionService;
    private static final Logger logger = CustomLogger.getLogger();

    public ProjectDiscussionServlet() {
        this.discussionService = new DiscussionService();
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
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
            return;
        }

        String projectId = pathInfo.substring(1);
        logger.info("Fetching discussions for projectId: " + projectId + " for user: " + userId);

        GenericResponse res;
        try {
            // Use the new method to get only discussions relevant to this user
            Discussion[] discussions = discussionService.getRelevantDiscussions(projectId, userId);
            res = new GenericResponse(discussions, true, null, null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching discussions", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching discussions: " + e.getMessage());
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(res));
    }
}
