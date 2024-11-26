package com.xployt.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
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

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.service.common.DiscussionService;
import com.xployt.util.CustomLogger;
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

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request is not multipart");
            return;
        }

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        try {
            List<FileItem> items = upload.parseRequest(request);
            String messageJson = null;
            List<File> uploadedFiles = new ArrayList<>();

            for (FileItem item : items) {
                if (item.isFormField()) {
                    if ("message".equals(item.getFieldName())) {
                        messageJson = item.getString();
                    }
                } else {
                    File file = File.createTempFile(item.getName().split("\\.")[0], "." + item.getName().split("\\.")[1], new File("uploads"));
                    
                    file.getParentFile().mkdirs();

                    try (InputStream inputStream = item.getInputStream()) {
                        Path outputPath = file.toPath();
                        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error writing file: {0}", e.getMessage());
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing file upload");
                        return;
                    }
                    
                    uploadedFiles.add(file);
                }
            }

            if (messageJson == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Message is required");
                return;
            }

            Gson gson = JsonUtil.useGson();
            Message message = gson.fromJson(messageJson, Message.class);
            logger.log(Level.INFO, "Message: {0}", message);

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
