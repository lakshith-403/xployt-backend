package com.xployt.controller.common;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.service.common.ProjectTeamService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;
import com.xployt.util.RequestProtocol;
/**
 * example: api/project/team/{projectId}
 * ProjectTeamServlet is responsible for handling requests to fetch the team for a specific project.
 * @pathParam {String} projectId - The ID of the project whose team is to be fetched.
 * @data: {
 *     projectId;
 *     PublicUser client;
 *     PublicUser projectLead;
 *     List<PublicUser> projectValidators;
 *     List<PublicUser> projectHackers;
 * }
 * @service {ProjectTeamService} - Service used to fetch the team for the project.
 **/
@WebServlet("/api/project/team/*")
public class ProjectTeamServlet extends HttpServlet {
    private ProjectTeamService projectTeamService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectTeamService = new ProjectTeamService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Fetching project team");

        if (!RequestProtocol.authenticateRequest(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
            return;
        }

        String projectId = pathInfo.substring(1);
        GenericResponse projectTeam;
        try {
            projectTeam = projectTeamService.fetchProjectTeam(projectId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project team");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(projectTeam));
    }
}
