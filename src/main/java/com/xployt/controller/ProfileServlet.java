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
        
        try {
            // TODO: Get userId from session/token
            int userId = 1; // Temporary hardcoded value
            Profile profile = profileService.getProfile(userId);
            
            GenericResponse genericResponse = new GenericResponse(profile, true, null, null);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String jsonResponse = JsonUtil.toJson(genericResponse);
            logger.info("jsonResponse is: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            logger.severe("Error fetching profile: " + e.getMessage());
            GenericResponse errorResponse = new GenericResponse(null, false, e.getMessage(), null);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(errorResponse));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        logger.info("Updating profile");
        
        try {
            String requestBody = request.getReader().lines()
                .reduce("", (accumulator, actual) -> accumulator + actual);
            Profile profile = JsonUtil.fromJson(requestBody, Profile.class);
            
            profileService.updateProfile(profile);
            
            GenericResponse genericResponse = new GenericResponse(null, true, "Profile updated successfully", null);
            
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            String jsonResponse = JsonUtil.toJson(genericResponse);
            logger.info("jsonResponse is: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            logger.severe("Error updating profile: " + e.getMessage());
            GenericResponse errorResponse = new GenericResponse(null, false, e.getMessage(), null);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(JsonUtil.toJson(errorResponse));
        }
    }
}