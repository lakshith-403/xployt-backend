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

import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseConfig;

public class SystemEarningsDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public void recordCommission(int reportId, int clientId, int hackerId, double amount, String description) throws SQLException {
        String sql = "INSERT INTO SystemEarnings (reportId, clientId, hackerId, amount, description, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reportId);
            stmt.setInt(2, clientId);
            stmt.setInt(3, hackerId);
            stmt.setDouble(4, amount);
            stmt.setString(5, description);
            
            stmt.executeUpdate();
            logger.info("Recorded system commission: " + amount + " for report: " + reportId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error recording system commission: {0}", e.getMessage());
            throw e;
        }
    }
    
    public List<Map<String, Object>> getAllEarnings() throws SQLException {
        String sql = "SELECT * FROM SystemEarnings ORDER BY timestamp DESC";
        List<Map<String, Object>> earnings = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> earning = new HashMap<>();
                earning.put("id", rs.getInt("id"));
                earning.put("reportId", rs.getInt("reportId"));
                earning.put("clientId", rs.getInt("clientId"));
                earning.put("hackerId", rs.getInt("hackerId"));
                earning.put("amount", rs.getDouble("amount"));
                earning.put("description", rs.getString("description"));
                earning.put("timestamp", rs.getTimestamp("timestamp").toString());
                earnings.add(earning);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching system earnings: {0}", e.getMessage());
            throw e;
        }
        
        return earnings;
    }
    
    public double getTotalEarnings() throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM SystemEarnings";
        double total = 0.0;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error calculating total system earnings: {0}", e.getMessage());
            throw e;
        }
        
        return total;
    }
    
    public List<Map<String, Object>> getEarningsByDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT * FROM SystemEarnings WHERE DATE(timestamp) BETWEEN ? AND ? ORDER BY timestamp DESC";
        List<Map<String, Object>> earnings = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> earning = new HashMap<>();
                earning.put("id", rs.getInt("id"));
                earning.put("reportId", rs.getInt("reportId"));
                earning.put("clientId", rs.getInt("clientId"));
                earning.put("hackerId", rs.getInt("hackerId"));
                earning.put("amount", rs.getDouble("amount"));
                earning.put("description", rs.getString("description"));
                earning.put("timestamp", rs.getTimestamp("timestamp").toString());
                earnings.add(earning);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching system earnings by date range: {0}", e.getMessage());
            throw e;
        }
        
        return earnings;
    }
} 