package com.xployt.dao.common;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.Profile;
import java.sql.*;
import javax.servlet.ServletContext;
import java.util.logging.Logger;
import com.xployt.util.JsonUtil;
public class ProfileDAO {
    private Logger logger = CustomLogger.getLogger();

    public Profile getProfile(int userId) {
        logger.info("ProfileDAO: Inside getProfile");
        String query = "SELECT u.userId, u.name, u.email, up.phone FROM Users u " +
                      "LEFT JOIN UserProfiles up ON u.userId = up.userId " +
                      "WHERE u.userId = ?";
                      
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            logger.info("ProfileDAO: Connection established");
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("ProfileDAO: Fetching profile for user: " + userId);
            
            if (rs.next()) {
                Profile profile = new Profile();
                profile.setUserId(rs.getInt("userId"));
                profile.setName(rs.getString("name"));
                profile.setEmail(rs.getString("email"));
                profile.setPhone(rs.getString("phone"));
                // profile.setProfilePicture(rs.getString("profile_picture"));
                // profile.setFundsRemaining(rs.getDouble("funds_remaining"));
                // profile.setFundsSpent(rs.getDouble("funds_spent"));
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
        String userQuery = "UPDATE Users SET name = ?, email = ? WHERE userId = ?";
        
        String profileQuery = "UPDATE UserProfiles SET phone = ? WHERE userId = ?";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
             PreparedStatement userStmt = conn.prepareStatement(userQuery);
             PreparedStatement profileStmt = conn.prepareStatement(profileQuery)) {
            
            userStmt.setString(1, profile.getName());
            userStmt.setString(2, profile.getEmail());
            userStmt.setInt(3, profile.getUserId());
            
            profileStmt.setString(1, profile.getPhone());
            profileStmt.setInt(2, profile.getUserId());
            
            int userRowsAffected = userStmt.executeUpdate();
            int profileRowsAffected = profileStmt.executeUpdate();
            String response = JsonUtil.toJson(profile);
            logger.info("ProfileDAO: Profile updated successfully: " + response);
            return userRowsAffected > 0 || profileRowsAffected > 0;
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