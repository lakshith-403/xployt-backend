package com.xployt.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/uploads/*")
public class FileServingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file specified");
                return;
            }

            String filename = pathInfo.substring(1);
            String uploadPath = getServletContext().getRealPath("/uploads");
            
            // Add debug logging
            System.out.println("Requested filename: " + filename);
            System.out.println("Upload path: " + uploadPath);
            
            File file = new File(uploadPath, filename);
            System.out.println("Full file path: " + file.getAbsolutePath());

            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + filename);
                return;
            }

            if (!file.canRead()) {
                System.out.println("Cannot read file: " + file.getAbsolutePath());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Cannot access file: " + filename);
                return;
            }

            String mimeType = getServletContext().getMimeType(file.getName());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            response.setContentType(mimeType);
            response.setContentLength((int) file.length());

            try (FileInputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error serving file: " + e.getMessage());
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request: " + e.getMessage());
        }
    }
}