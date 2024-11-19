package com.xployt.dao;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.Profile;
import java.sql.*;
import javax.servlet.ServletContext;
import java.util.logging.Logger;

public class ProfileDAO {
    private Logger logger = CustomLogger.getLogger();

    public Profile getProfile(int userId) {
        logger.info("ProfileDAO: Inside getProfile");
        String query = "SELECT u.*, c.funds_remaining, c.funds_spent FROM users u " +
                      "LEFT JOIN clients c ON u.user_id = c.client_id " +
                      "WHERE u.user_id = ?";
                      
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            logger.info("ProfileDAO: Connection established");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("ProfileDAO: Fetching profile for user: " + userId);
            
            if (rs.next()) {
                Profile profile = new Profile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setName(rs.getString("name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhoneNumber(rs.getString("phone"));
                profile.setProfilePicture(rs.getString("profile_picture"));
                profile.setFundsRemaining(rs.getDouble("funds_remaining"));
                profile.setFundsSpent(rs.getDouble("funds_spent"));
                logger.info("ProfileDAO: Profile fetched successfully");
                return profile;
            }
            logger.info("ProfileDAO: No profile found for user: " + userId);
            return null;
        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error fetching profile: " + e.getMessage());
            return null;
        }
    }

    public boolean updateProfile(Profile profile) {
        logger.info("ProfileDAO: Inside updateProfile");
        String query = "UPDATE users SET name = ?, email = ?, phone = ? WHERE user_id = ?";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            logger.info("ProfileDAO: Connection established");
            stmt.setString(1, profile.getName());
            stmt.setString(2, profile.getEmail());
            stmt.setString(3, profile.getPhoneNumber());
            stmt.setInt(4, profile.getUserId());
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("ProfileDAO: Profile updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error updating profile: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProfilePicture(int userId, String pictureUrl) {
        logger.info("ProfileDAO: Inside updateProfilePicture");
        String query = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            logger.info("ProfileDAO: Connection established");
            stmt.setString(1, pictureUrl);
            stmt.setInt(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            logger.info("ProfileDAO: Profile picture updated successfully. Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error updating profile picture: " + e.getMessage());
            return false;
        }
    }
}