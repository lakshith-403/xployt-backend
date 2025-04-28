package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.model.User;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseConfig;

public class UserDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public User createUser(User user) throws SQLException {

        // ServletContext servletContext = ContextManager.getContext("DBConnection");
        // @SuppressWarnings("resource")
        Connection conn = DatabaseConfig.getConnection();
        conn.setAutoCommit(false);
        try {
            String sql = "INSERT INTO Users (email, passwordHash, name, role) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getRole());
            stmt.executeUpdate();

            sql = "SELECT userId FROM Users WHERE email = ?";
            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, user.getEmail());
            ResultSet rs = stmt2.executeQuery();
            rs.next();
            String userId = rs.getString("userId");
            logger.info("User created successfully. User ID: " + userId);
            conn.commit();
            return new User(userId, user.getEmail(), user.getPasswordHash(), user.getName(), user.getRole(), null,
                    null, "active");
        } catch (SQLException e) {
            conn.rollback();
            logger.log(Level.SEVERE, "Error creating user: {0}", e.getMessage());
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deleteUser(String userId) throws SQLException {
        String sql = "DELETE FROM Users WHERE userId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.log(Level.INFO, "User not found: {0}", userId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting user: {0}", e.getMessage());
            throw e;
        }
    }

    public User getUserById(String userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE userId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("userId"),
                        rs.getString("email"),
                        rs.getString("passwordHash"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user by ID: {0}", e.getMessage());
            throw e;
        }
        return null;
    }

    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("userId"),
                        rs.getString("email"),
                        rs.getString("passwordHash"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("createdAt"),
                        rs.getString("updatedAt"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user by email: {0}", e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Map<String, Object>> getHackerProjects(int hackerId) throws SQLException {
        List<Map<String, Object>> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT p.projectId, p.title, p.state " +
                    "FROM Projects p " +
                    "JOIN ProjectHackers ph ON p.projectId = ph.projectId " +
                    "WHERE ph.hackerId = ? " +
                    "AND (p.state = 'Active' OR p.state = 'Completed')";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, hackerId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> project = new HashMap<>();
                project.put("projectId", rs.getInt("projectId"));
                project.put("title", rs.getString("title"));
                project.put("state", rs.getString("state"));
                projects.add(project);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching hacker projects: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return projects;
    }
    
    public List<Map<String, Object>> getClientProjects(int clientId) throws SQLException {
        List<Map<String, Object>> projects = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT projectId, title, state " +
                    "FROM Projects " +
                    "WHERE clientId = ? " +
                    "AND (state = 'Active' OR state = 'Completed')";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, clientId);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> project = new HashMap<>();
                project.put("projectId", rs.getInt("projectId"));
                project.put("title", rs.getString("title"));
                project.put("state", rs.getString("state"));
                projects.add(project);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching client projects: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return projects;
    }

    // Update user password
    public void updateUserPassword(String userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE Users SET passwordHash = ? WHERE userId = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setString(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating password failed, no user with ID: " + userId);
            }
            
            logger.info("Password updated for user ID: " + userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating password: {0}", e.getMessage());
            throw e;
        }
    }
}
