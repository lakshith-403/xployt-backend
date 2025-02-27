package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.service.common.UserProjectsService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/projects/*")
/**
 * example: api/projects?userId=1&userStatus=client
 * UserProjects servlet is responsible for handling requests to fetch projects for a specific user.
 * @queryParam {String} userId - The ID of the user whose projects are to be fetched.
 * @queryParam {String} userStatus - The status of the user whose projects are to be fetched.
 * @data: projects: {
 *      active: [],
 *      inactive: []
 *      requested: []
 *     }
 * @service {UserProjectsService} - Service used to fetch projects for the user.
 **/
public class UserProjects extends HttpServlet {
    private UserProjectsService projectsService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public  void init() throws ServletException {
        projectsService = new UserProjectsService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Fetching projects for Client");

        String userId = request.getParameter("userId");
        String userStatus = request.getParameter("userStatus");

        if(userId == null || userId.isEmpty() || userStatus == null || userStatus.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID or User Status not provided");
            return;
        }

        GenericResponse projects;
        try {
            projects = projectsService.fetchUserProjects(userId, userStatus);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching projects");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(projects));

        logger.info("Projects fetched successfully");
    }
}
