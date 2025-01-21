package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided hehe hoo");
            return;
        }

        String projectId = pathInfo.substring(1); // Get userId from URL
        logger.info("Fetching discussions for projectId: " + projectId);

        GenericResponse res;
        try {
            res = discussionService.fetchDiscussions(projectId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching discussions");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(res));
    }
}
