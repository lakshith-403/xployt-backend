package com.xployt.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.service.common.DiscussionService;
import com.xployt.util.CustomLogger;
import com.xployt.util.FileUploadUtil;
import com.xployt.util.JsonUtil;

@WebServlet("/api/messages/*")
@MultipartConfig
public class MessageServlet extends HttpServlet {
    private DiscussionService discussionService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        discussionService = new DiscussionService();
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Sending message");

        try {
            // Process multipart request
            FileUploadUtil.UploadResult uploadResult = FileUploadUtil.processMultipartRequest(request, response);
            if (uploadResult == null) {
                return; // Error already sent to client
            }
            
            String messageJson = uploadResult.getFormField("message");
            if (messageJson == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message is required");
                return;
            }

            Gson gson = JsonUtil.useGson();
            Message message = gson.fromJson(messageJson, Message.class);
            logger.log(Level.INFO, "Message: {0}", message);

            // Extract attachment IDs from message
            List<String> attachmentIds = message.getAttachments().stream()
                .map(attachment -> attachment.getId())
                .collect(Collectors.toList());

            // Process file attachments with the new generic method
            List<File> uploadedFiles = FileUploadUtil.processAttachments(
                uploadResult.getFileItems(),
                attachmentIds,
                getServletContext(), 
                response
            );

            GenericResponse result = discussionService.sendMessage(message);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.useGson().toJson(result));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request");
            logger.log(Level.SEVERE, "Error processing request: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Updating message");

        String messageId = request.getPathInfo().substring(1);
        String messageJson = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        if (messageId == null || messageId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message ID is required");
            return;
        }

        Gson gson = JsonUtil.useGson();
        Message message = gson.fromJson(messageJson, Message.class);

        try {
            GenericResponse result = discussionService.updateMessage(message);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.useGson().toJson(result));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating message");
            logger.log(Level.SEVERE, "Error updating message: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        logger.info("Deleting message");

        String messageId = request.getPathInfo().substring(1);

        if (messageId == null || messageId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message ID is required");
            return;
        }

        try {
            GenericResponse result = discussionService.deleteMessage(messageId);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.useGson().toJson(result));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting message");
            logger.log(Level.SEVERE, "Error deleting message: {0}", e.getMessage());
            e.printStackTrace();
        }
    }
}
