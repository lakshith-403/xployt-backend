package com.xployt.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.service.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/api/messages/*")
public class MessageServlet extends HttpServlet {
    private DiscussionService discussionService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        discussionService = new DiscussionService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Sending message");

        GsonBuilder builder = new GsonBuilder(); 
        builder.setPrettyPrinting(); 
        Gson gson = builder.create();

        Message message = gson.fromJson(request.getReader(), Message.class);
        
        GenericResponse result;
        try {
            result = discussionService.sendMessage(message);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending message");
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(result));
    }
}
