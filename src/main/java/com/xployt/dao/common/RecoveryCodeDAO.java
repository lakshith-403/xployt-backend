package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.model.RecoveryCode;
import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseConfig;

public class RecoveryCodeDAO {
    private static final Logger logger = CustomLogger.getLogger();

    // Create tables if not exists
    public void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS RecoveryCodes (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL, " +
                "code VARCHAR(8) NOT NULL, " +
                "createdAt TIMESTAMP NOT NULL)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
            logger.info("RecoveryCodes table created or already exists");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating RecoveryCodes table: {0}", e.getMessage());
            throw e;
        }
    }

    // Generate and store a new recovery code
    public RecoveryCode createRecoveryCode(String email) throws SQLException {
        // First, delete any existing codes for this email
        deleteByEmail(email);
        
        // Generate a random 6-digit code
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        String sql = "INSERT INTO RecoveryCodes (id, email, code, createdAt) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, email);
            stmt.setString(3, code);
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating recovery code failed, no rows affected.");
            }
            
            logger.info("Recovery code created for email: " + email);
            return new RecoveryCode(id, email, code, now);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating recovery code: {0}", e.getMessage());
            throw e;
        }
    }

    // Get recovery code by email and code
    public RecoveryCode getByEmailAndCode(String email, String code) throws SQLException {
        String sql = "SELECT * FROM RecoveryCodes WHERE email = ? AND code = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, code);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("id");
                    LocalDateTime createdAt = rs.getTimestamp("createdAt").toLocalDateTime();
                    
                    return new RecoveryCode(id, email, code, createdAt);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting recovery code: {0}", e.getMessage());
            throw e;
        }
        return null;
    }

    // Delete recovery code by email
    public void deleteByEmail(String email) throws SQLException {
        String sql = "DELETE FROM RecoveryCodes WHERE email = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
            logger.info("Deleted recovery codes for email: " + email);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting recovery codes: {0}", e.getMessage());
            throw e;
        }
    }

    // Delete recovery code by id
    public void deleteById(String id) throws SQLException {
        String sql = "DELETE FROM RecoveryCodes WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
            logger.info("Deleted recovery code with id: " + id);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting recovery code: {0}", e.getMessage());
            throw e;
        }
    }
    
    // Clean up expired recovery codes (older than 5 minutes)
    public void cleanupExpiredCodes() throws SQLException {
        String sql = "DELETE FROM RecoveryCodes WHERE createdAt < ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusMinutes(5)));
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                logger.info("Cleaned up " + deleted + " expired recovery codes");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error cleaning up expired recovery codes: {0}", e.getMessage());
            throw e;
        }
    }
} 