package com.xployt.controller;

import com.xployt.model.Profile;
import com.xployt.service.ProfileService;
import spark.Request;
import spark.Response;
import com.google.gson.Gson;

public class ProfileController {
    private ProfileService profileService;
    private Gson gson;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
        this.gson = new Gson();
    }

    public String getProfile(Request req, Response res) {
        try {
            int userId = Integer.parseInt(req.params("userId"));
            Profile profile = profileService.getProfile(userId);
            
            if (profile != null) {
                return gson.toJson(profile);
            } else {
                res.status(404);
                return gson.toJson("Profile not found");
            }
        } catch (Exception e) {
            res.status(500);
            return gson.toJson("Internal server error: " + e.getMessage());
        }
    }

    public String updateProfile(Request req, Response res) {
        try {
            Profile profile = gson.fromJson(req.body(), Profile.class);
            profileService.updateProfile(profile);
            return gson.toJson("Profile updated successfully");
        } catch (Exception e) {
            res.status(500);
            return gson.toJson("Internal server error: " + e.getMessage());
        }
    }
}