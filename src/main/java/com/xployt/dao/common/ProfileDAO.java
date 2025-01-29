package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.model.Profile;
import com.xployt.util.ContextManager; // Add this import
import com.xployt.util.CustomLogger; // Add this import

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
        Connection conn = (Connection) ContextManager.getContext("DBConnection").getAttribute("DBConnection");
        try (
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
            ServletContext servletContext = ContextManager.getContext("DBConnection");
            conn = (Connection) servletContext.getAttribute("DBConnection");
            conn.setAutoCommit(false);

            // Update Users table
            String userSql = "UPDATE Users SET name = ?, email = ? WHERE userId = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userSql)) {
                userStmt.setString(1, profile.getName());
                userStmt.setString(2, profile.getEmail());
                userStmt.setInt(3, profile.getUserId());
                int rowsAffected = userStmt.executeUpdate();
                if (rowsAffected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Check if UserProfile exists
            String checkSql = "SELECT profileId FROM UserProfiles WHERE userId = ?";
            int profileId = -1;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, profile.getUserId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    profileId = rs.getInt("profileId");
                }
            }

            if (profileId != -1) {
                // Update existing profile
                String updateSql = "UPDATE UserProfiles SET " +
                        "phone = ?, username = ?, firstName = ?, " +
                        "lastName = ?, companyName = ?, dob = ?, " +
                        "linkedIn = ? WHERE profileId = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, profile.getPhone());
                    updateStmt.setString(2, profile.getUsername());
                    updateStmt.setString(3, profile.getFirstName());
                    updateStmt.setString(4, profile.getLastName());
                    updateStmt.setString(5, profile.getCompanyName());
                    updateStmt.setDate(6, profile.getDob() != null ? java.sql.Date.valueOf(profile.getDob()) : null);
                    updateStmt.setString(7, profile.getLinkedIn());
                    updateStmt.setInt(8, profileId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Insert new profile
                String insertSql = "INSERT INTO UserProfiles (userId, phone, username, " +
                        "firstName, lastName, companyName, dob, linkedIn) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, profile.getUserId());
                    insertStmt.setString(2, profile.getPhone());
                    insertStmt.setString(3, profile.getUsername());
                    insertStmt.setString(4, profile.getFirstName());
                    insertStmt.setString(5, profile.getLastName());
                    insertStmt.setString(6, profile.getCompanyName());
                    insertStmt.setDate(7, profile.getDob() != null ? java.sql.Date.valueOf(profile.getDob()) : null);
                    insertStmt.setString(8, profile.getLinkedIn());
                    insertStmt.executeUpdate();
                }
            }

            conn.commit();
            conn.close();
            return true;

        } catch (SQLException e) {
            logger.severe("ProfileDAO: Error updating profile: " + e.getMessage());
            try {
                if (conn != null)
                    conn.rollback();
            } catch (SQLException ex) {
                logger.severe("Error rolling back transaction: " + ex.getMessage());
            }
            throw new RuntimeException("Failed to update profile", e);
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.severe("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}