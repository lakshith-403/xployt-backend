package com.xployt.service;

import com.xployt.dao.common.ProfileDAO;
import com.xployt.model.Profile;
import com.xployt.util.CustomLogger;

import java.util.logging.Logger;

public class ProfileService {
    private ProfileDAO profileDAO;
    private static final Logger logger = CustomLogger.getLogger();
    
     
    public ProfileService() {
        this.profileDAO = new ProfileDAO();
    }

    public Profile getProfile(int userId) throws Exception {
        try {
            logger.info("Profile Service: Fetching profile for userId: " + userId);
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