package com.xployt.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.service.common.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

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

        Gson gson = JsonUtil.useGson();
        
        logger.info("Request body: " + request.getReader().readLine());
        Message message = gson.fromJson(request.getReader(), Message.class);
        logger.info("Message: " + message);
        
        GenericResponse result;
        try {
            result = discussionService.sendMessage(message);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error sending message");
            logger.severe("Error sending message: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.useGson().toJson(result));
    }
}
