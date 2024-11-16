package com.xployt.service;

import com.xployt.dao.ProfileDAO;
import com.xployt.model.Profile;
import java.sql.SQLException;

public class ProfileService {
    private ProfileDAO profileDAO;

    public ProfileService(ProfileDAO profileDAO) {
        this.profileDAO = profileDAO;
    }

    public Profile getProfile(int userId) throws SQLException {
        return profileDAO.getProfileById(userId);
    }

    public void updateProfile(Profile profile) throws SQLException {
        profileDAO.updateProfile(profile);
    }
}
