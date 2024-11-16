package com.xployt.dao;

import com.xployt.model.Profile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfileDAO {
    private Connection connection;

    public ProfileDAO(Connection connection) {
        this.connection = connection;
    }

    public Profile getProfileById(int userId) throws SQLException {
        String query = "SELECT * FROM profiles WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Profile(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getString("profile_picture"),
                    rs.getDouble("remaining_funds"),
                    rs.getDouble("spent_funds")
                );
            }
            return null;
        }
    }

    public void updateProfile(Profile profile) throws SQLException {
        String query = "UPDATE profiles SET name = ?, email = ?, phone_number = ?, " +
                      "profile_picture = ?, remaining_funds = ?, spent_funds = ? " +
                      "WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getEmail());
            stmt.setString(3, profile.getPhoneNumber());
            stmt.setString(4, profile.getProfilePicture());
            stmt.setDouble(5, profile.getRemainingFunds());
            stmt.setDouble(6, profile.getSpentFunds());
            stmt.setInt(7, profile.getId());
            
            stmt.executeUpdate();
        }
    }
}