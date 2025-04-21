package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.model.FinanceTransaction;
import com.xployt.util.CustomLogger;
import com.xployt.util.DatabaseConfig;

public class FinanceDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public double getUserBalance(int userId) throws SQLException {
        String sql = "SELECT balance FROM UserFinance WHERE userId = ?";
        double balance = 0.0;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                balance = rs.getDouble("balance");
            } else {
                // Initialize user finance record if not exists
                initializeUserFinance(userId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user balance: {0}", e.getMessage());
            throw e;
        }
        
        return balance;
    }
    
    private void initializeUserFinance(int userId) throws SQLException {
        String sql = "INSERT INTO UserFinance (userId, balance) VALUES (?, 0.0)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            logger.info("Initialized finance record for user: " + userId);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error initializing user finance: {0}", e.getMessage());
            throw e;
        }
    }
    
    public void addFunds(int userId, double amount, String description) throws SQLException {
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Update user balance
            String updateBalanceSql = "UPDATE UserFinance SET balance = balance + ? WHERE userId = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql);
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, userId);
            int affectedRows = updateStmt.executeUpdate();
            
            if (affectedRows == 0) {
                // Initialize user finance if not exists and retry
                initializeUserFinance(userId);
                updateStmt.executeUpdate();
            }
            
            // Record transaction
            String insertTransactionSql = "INSERT INTO FinanceTransactions (userId, amount, type, description, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement insertStmt = conn.prepareStatement(insertTransactionSql);
            insertStmt.setInt(1, userId);
            insertStmt.setDouble(2, amount);
            insertStmt.setString(3, "DEPOSIT");
            insertStmt.setString(4, description);
            insertStmt.executeUpdate();
            
            conn.commit();
            logger.info("Added funds to user: " + userId + ", amount: " + amount);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction: {0}", ex.getMessage());
                }
            }
            logger.log(Level.SEVERE, "Error adding funds: {0}", e.getMessage());
            throw e;
        }
    }
    
    public void withdrawFunds(int userId, double amount, String description) throws SQLException {
        Connection conn = null;
        
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Check if user has sufficient balance
            double currentBalance = getUserBalance(userId);
            if (currentBalance < amount) {
                throw new SQLException("Insufficient funds");
            }
            
            // Update user balance
            String updateBalanceSql = "UPDATE UserFinance SET balance = balance - ? WHERE userId = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateBalanceSql);
            updateStmt.setDouble(1, amount);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();
            
            // Record transaction
            String insertTransactionSql = "INSERT INTO FinanceTransactions (userId, amount, type, description, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            PreparedStatement insertStmt = conn.prepareStatement(insertTransactionSql);
            insertStmt.setInt(1, userId);
            insertStmt.setDouble(2, amount);
            insertStmt.setString(3, "WITHDRAWAL");
            insertStmt.setString(4, description);
            insertStmt.executeUpdate();
            
            conn.commit();
            logger.info("Withdrawn funds from user: " + userId + ", amount: " + amount);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction: {0}", ex.getMessage());
                }
            }
            logger.log(Level.SEVERE, "Error withdrawing funds: {0}", e.getMessage());
            throw e;
        }
    }
    
    public List<FinanceTransaction> getUserTransactions(int userId) throws SQLException {
        String sql = "SELECT * FROM FinanceTransactions WHERE userId = ? ORDER BY timestamp DESC";
        List<FinanceTransaction> transactions = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FinanceTransaction transaction = new FinanceTransaction(
                    rs.getInt("transactionId"),
                    rs.getInt("userId"),
                    rs.getDouble("amount"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getTimestamp("timestamp").toString()
                );
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching user transactions: {0}", e.getMessage());
            throw e;
        }
        
        return transactions;
    }
} 