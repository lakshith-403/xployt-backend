package com.xployt.controller;

import com.xployt.model.GenericResponse;
import com.xployt.service.ProjectHackerService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import com.xployt.util.CustomLogger;

@WebServlet("api/hacker/invitations/*")

public class ProjectHackerServlet extends HttpServlet {
    private ProjectHackerService projectHackerService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        projectHackerService = new ProjectHackerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        logger.info("Servlet: Fetching project hackers");
        String pathInfo = req.getPathInfo();
//        if (pathInfo == null || pathInfo.isEmpty()) {
//            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path info");
//            return;
//        }
//        String userId = (String) req.getSession().getAttribute("userId");

        GenericResponse projectHackers;

        try {
            projectHackers = projectHackerService.fetchProjectHackers();
            logger.info("Project Hackers: " + projectHackers);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching project Hackers");
            return;
        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String jsonResponse = JsonUtil.toJson(projectHackers);
        logger.info(jsonResponse);
        res.getWriter().write(jsonResponse);

    }
}

