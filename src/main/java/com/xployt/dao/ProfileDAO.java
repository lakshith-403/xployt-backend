package com.xployt.dao;

import com.xployt.model.Profile;
import java.sql.*;

public class ProfileDAO {
    private Connection connection;

    public ProfileDAO(Connection connection) {
        this.connection = connection;
    }

    public Profile getProfile(int userId) throws SQLException {
        String query = "SELECT u.*, c.funds_remaining, c.funds_spent FROM users u " +
                      "LEFT JOIN clients c ON u.user_id = c.client_id " +
                      "WHERE u.user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Profile profile = new Profile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setName(rs.getString("name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhoneNumber(rs.getString("phone"));
                profile.setProfilePicture(rs.getString("profile_picture"));
                profile.setFundsRemaining(rs.getDouble("funds_remaining"));
                profile.setFundsSpent(rs.getDouble("funds_spent"));
                return profile;
            }
            return null;
        }
    }

    public void updateProfile(Profile profile) throws SQLException {
        String query = "UPDATE users SET name = ?, email = ?, phone = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getEmail());
            stmt.setString(3, profile.getPhoneNumber());
            stmt.setInt(4, profile.getUserId());
            stmt.executeUpdate();
        }
    }

    public void updateProfilePicture(int userId, String pictureUrl) throws SQLException {
        String query = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, pictureUrl);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}