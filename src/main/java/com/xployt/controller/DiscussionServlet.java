package com.xployt.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.service.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder; 

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
        logger.info("Fetching discussions");
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Project ID not provided");
            return;
        }

        String projectId = pathInfo.substring(1);
        GenericResponse discussions;
        try {
            discussions = discussionService.fetchDiscussions(projectId);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching discussions");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(discussions));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Creating discussion");

        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();

        Discussion discussion = gson.fromJson(request.getReader(), Discussion.class);
        
        GenericResponse result;
        try {
            result = discussionService.createDiscussion(discussion);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating discussion");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(result));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Updating discussion");

        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();

        Discussion discussion = gson.fromJson(request.getReader(), Discussion.class);
        GenericResponse result;
        try {
            result = discussionService.updateDiscussion(discussion);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating discussion");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(result));
    }
} 
