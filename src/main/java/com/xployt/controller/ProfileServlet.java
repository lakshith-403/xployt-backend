package com.xployt.controller;

import com.xployt.model.Profile;
import com.xployt.model.GenericResponse;
import com.xployt.service.ProfileService;
import com.xployt.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/profile/*")
public class ProfileServlet extends HttpServlet {
    private ProfileService profileService;
    private JsonUtil jsonUtil;

    @Override
    public void init() throws ServletException {
        this.profileService = new ProfileService();
        this.jsonUtil = new JsonUtil();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            // TODO: Get userId from session/token
            int userId = 1; // Temporary hardcoded value
            Profile profile = profileService.getProfile(userId);
            
            // Updated to match GenericResponse constructor
            GenericResponse response = new GenericResponse(profile, true, null, null);
            jsonUtil.writeJson(resp, response);
        } catch (Exception e) {
            // Updated error response
            GenericResponse response = new GenericResponse(null, false, e.getMessage(), e.getStackTrace().toString());
            jsonUtil.writeJson(resp, response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            Profile profile = jsonUtil.readJson(req, Profile.class);
            profileService.updateProfile(profile);
            
            // Updated to match GenericResponse constructor
            GenericResponse response = new GenericResponse(null, true, null, null);
            jsonUtil.writeJson(resp, response);
        } catch (Exception e) {
            // Updated error response
            GenericResponse response = new GenericResponse(null, false, e.getMessage(), e.getStackTrace().toString());
            jsonUtil.writeJson(resp, response);
        }
    }
}