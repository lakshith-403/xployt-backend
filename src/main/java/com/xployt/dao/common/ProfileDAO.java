package com.xployt.dao.common;

import com.xployt.model.Profile;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;  // Add this import
import java.sql.Date;  // Add this import

public class ProfileDAO {
    private static final Logger logger = CustomLogger.getLogger();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public Profile getProfile(int userId) {
        String sql = "SELECT u.userId, u.name, u.email, up.phone, " +
                    "up.username, up.firstName, up.lastName, up.companyName, " +
                    "up.dob, up.linkedIn " +
                    "FROM Users u " +
                    "LEFT JOIN UserProfiles up ON u.userId = up.userId " +
                    "WHERE u.userId = ?";

        try (Connection conn = (Connection) ContextManager.getContext("DBConnection").getAttribute("DBConnection");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Profile profile = new Profile();
                    profile.setUserId(rs.getInt("userId"));
                    profile.setName(rs.getString("name"));
                    profile.setEmail(rs.getString("email"));
                    profile.setPhone(rs.getString("phone"));
                    profile.setUsername(rs.getString("username"));
                    profile.setFirstName(rs.getString("firstName"));
                    profile.setLastName(rs.getString("lastName"));
                    profile.setCompanyName(rs.getString("companyName"));
                    
                    // Handle date conversion
                    Date dob = rs.getDate("dob");
                    if (dob != null) {
                        profile.setDob(DATE_FORMAT.format(dob));
                    }
                    
                    profile.setLinkedIn(rs.getString("linkedIn"));
                    
                    logger.info("ProfileDAO: Profile fetched successfully for userId: " + userId);
                    return profile;
                }
            }
            logger.info("ProfileDAO: No profile found for userId: " + userId);
        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error fetching profile: " + e.getMessage());
        }
        return null;
    }

    // Update method should also handle date conversion
    public boolean updateProfile(Profile profile) {
        Connection conn = null;
        try {
            // Get a fresh connection each time
            ServletContext servletContext = ContextManager.getContext("DBConnection");
            conn = (Connection) servletContext.getAttribute("DBConnection");
            
            if (conn == null || conn.isClosed()) {
                logger.severe("ProfileDAO: Database connection is null or closed");
                return false;
            }
            
            conn.setAutoCommit(false);
    
            // Update Users table
            String userSql = "UPDATE Users SET name = ?, email = ? WHERE userId = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, profile.getName());
                userStmt.setString(2, profile.getEmail());
                userStmt.setInt(3, profile.getUserId());
                int userRowsAffected = userStmt.executeUpdate();
                logger.info("Users table update affected " + userRowsAffected + " rows");
                
                if (userRowsAffected == 0) {
                    logger.warning("No user found with ID: " + profile.getUserId());
                    conn.rollback();
                    return false;
                }
            }
    
            // Update UserProfiles table
            String profileSql = "INSERT INTO UserProfiles (userId, phone) VALUES (?, ?) " +
                              "ON DUPLICATE KEY UPDATE phone = ?";
            try (PreparedStatement profileStmt = conn.prepareStatement(profileSql)) {
                profileStmt.setInt(1, profile.getUserId());
                profileStmt.setString(2, profile.getPhone());
                profileStmt.setString(3, profile.getPhone());
                int profileRowsAffected = profileStmt.executeUpdate();
                logger.info("UserProfiles table update affected " + profileRowsAffected + " rows");
            }
    
            conn.commit();
            logger.info("Profile updated successfully for userId: " + profile.getUserId());
            return true;
    
        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error updating profile: " + e.getMessage());
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.severe("ProfileDAO: Error rolling back transaction: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.severe("ProfileDAO: Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}