package com.xployt.controller.client;

import com.xployt.model.GenericResponse;
import com.xployt.service.client.HackerFilterService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import com.xployt.util.CustomLogger;

@WebServlet("/api/invitations/filter/*")
public class HackerFilterServlet extends HttpServlet {
    private HackerFilterService hackerFilterService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        hackerFilterService = new HackerFilterService();
    }

    // handles GET requests to filter hacker suggestions for a specific project
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("HackerFilterServlet: Filtering hackers");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || !pathInfo.startsWith("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path info");
            return;
        }
        String projectId = pathInfo.substring(1);

        GenericResponse hackers;

        try {
            logger.info("Trying to fetch hackers for project: " + projectId);
            hackers = hackerFilterService.filterHackers(projectId);
            logger.info("Hackers: " + hackers);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching hackers");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(hackers));
    }


}
