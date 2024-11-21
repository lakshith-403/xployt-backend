package com.xployt.controller;

import com.xployt.model.Profile;
import com.xployt.model.GenericResponse;
import com.xployt.service.ProfileService;
import com.xployt.util.JsonUtil;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet("/api/profile/*")
public class ProfileServlet extends HttpServlet {
    private ProfileService profileService;
    private static final Logger logger = CustomLogger.getLogger();

    @Override
    public void init() throws ServletException {
        profileService = new ProfileService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        logger.info("Fetching profile for user");
        String pathInfo = request.getPathInfo();
        
        // Validate path info
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID not provided");
            return;
        }

        try {
            int userId = Integer.parseInt(pathInfo.substring(1));
            logger.info("Parsed userId: " + userId);
            
            Profile profile = profileService.getProfile(userId);
            logger.info("Retrieved profile: " + profile);

            if (profile == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Profile not found");
                return;
            }

            GenericResponse genericResponse = new GenericResponse(profile, true, null, null);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String jsonResponse = JsonUtil.toJson(genericResponse);
            logger.info("jsonResponse is: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (NumberFormatException e) {
            logger.warning("Invalid user ID format: " + pathInfo);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            logger.severe("Error fetching profile: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching profile");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        logger.info("Updating profile");
        
        try {
            // Read request body
            String requestBody = request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual);
            
            if (requestBody.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request body is empty");
                return;
            }

            // Parse profile from request body
            Profile profile = JsonUtil.fromJson(requestBody, Profile.class);
            if (profile == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid profile data");
                return;
            }
            
            profileService.updateProfile(profile);
            
              
            // Send success response
            GenericResponse genericResponse = new GenericResponse(null, true, "Profile updated successfully", null);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String jsonResponse = JsonUtil.toJson(genericResponse);
            logger.info("jsonResponse is: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            logger.severe("Error updating profile: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating profile");
        }
    }
}