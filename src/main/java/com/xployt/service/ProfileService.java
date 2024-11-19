package com.xployt.service;

import com.xployt.dao.ProfileDAO;
import com.xployt.model.Profile;
import com.xployt.util.ContextManager;

public class ProfileService {
    private ProfileDAO profileDAO;

    public ProfileService() {
        this.profileDAO = new ProfileDAO(ContextManager.getConnection());
    }

    public Profile getProfile(int userId) throws Exception {
        try {
            return profileDAO.getProfile(userId);
        } catch (Exception e) {
            throw new Exception("Error fetching profile: " + e.getMessage());
        }
    }

    public void updateProfile(Profile profile) throws Exception {
        try {
            profileDAO.updateProfile(profile);
        } catch (Exception e) {
            throw new Exception("Error updating profile: " + e.getMessage());
        }
    }

    public void updateProfilePicture(int userId, String pictureUrl) throws Exception {
        try {
            profileDAO.updateProfilePicture(userId, pictureUrl);
        } catch (Exception e) {
            throw new Exception("Error updating profile picture: " + e.getMessage());
        }
    }
}