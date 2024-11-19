package com.xployt.controller;

import com.xployt.model.Profile;
import com.xployt.model.GenericResponse;
import com.xployt.service.ProfileService;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;

@WebServlet("/api/profile/*")
public class ProfileServlet extends HttpServlet {
    private ProfileService profileService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        this.profileService = new ProfileService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        try {
            // TODO: Get userId from session/token
            int userId = 1; // Temporary hardcoded value
            Profile profile = profileService.getProfile(userId);
            
            String jsonResponse = gson.toJson(new GenericResponse<Profile>(
                true,
                "Profile retrieved successfully",
                profile,
                null
            ));
            
            resp.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            String jsonResponse = gson.toJson(new GenericResponse<Profile>(
                false,
                "Failed to retrieve profile",
                null,
                e.getMessage()
            ));
            
            resp.getWriter().write(jsonResponse);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        
        try {
            // Read the request body
            BufferedReader reader = req.getReader();
            Profile profile = gson.fromJson(reader, Profile.class);
            
            profileService.updateProfile(profile);
            
            String jsonResponse = gson.toJson(new GenericResponse<Void>(
                true,
                "Profile updated successfully",
                null,
                null
            ));
            
            resp.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            String jsonResponse = gson.toJson(new GenericResponse<Void>(
                false,
                "Failed to update profile",
                null,
                e.getMessage()
            ));
            
            resp.getWriter().write(jsonResponse);
        }
    }
}