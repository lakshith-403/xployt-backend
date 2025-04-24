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

public class ProjectFinanceDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public List<Map<String, Object>> getProjectReportPayments(int projectId) throws SQLException {
        List<Map<String, Object>> reports = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();

            // SQL to fetch report details with payment information
            String sql = "SELECT r.reportId, r.hackerId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, " +
                    "pla.amount AS payment_amount, " +
                    "CASE WHEN pp.paymentId IS NULL THEN false ELSE true END AS paid " +
                    "FROM BugReports r " +
                    "JOIN Projects p ON r.projectId = p.projectId " +
                    "JOIN PaymentLevelAmounts pla ON p.projectId = pla.projectId AND r.severity = pla.level " +
                    "LEFT JOIN ProjectPayments pp ON r.reportId = pp.reportId " +
                    "WHERE r.projectId = ? " +
                    "ORDER BY r.createdAt DESC";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getInt("reportId"));
                report.put("hackerId", rs.getInt("hackerId"));
                report.put("severity", rs.getString("severity"));
                report.put("vulnerabilityType", rs.getString("vulnerabilityType"));
                report.put("title", rs.getString("title"));
                report.put("createdAt", rs.getTimestamp("createdAt").toString());
                report.put("status", rs.getString("status"));
                report.put("payment_amount", rs.getDouble("payment_amount"));
                report.put("paid", rs.getBoolean("paid"));
                reports.add(report);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching project report payments: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing ResultSet: {0}", e.getMessage());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing PreparedStatement: {0}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing Connection: {0}", e.getMessage());
                }
            }
        }

        return reports;
    }

    public boolean verifyProjectClient(int projectId, int clientId) throws SQLException {
        boolean isClient = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT 1 FROM Projects WHERE projectId = ? AND clientId = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            stmt.setInt(2, clientId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                isClient = true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error verifying project client: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing ResultSet: {0}", e.getMessage());
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing PreparedStatement: {0}", e.getMessage());
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Error closing Connection: {0}", e.getMessage());
                }
            }
        }

        return isClient;
    }

    public Map<String, Object> getReportPaymentDetails(int reportId) throws SQLException {
        Map<String, Object> reportDetails = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT r.reportId, r.projectId, r.hackerId, r.severity, r.vulnerabilityType, r.title, r.createdAt, r.status, " +
                    "pla.amount AS payment_amount, " +
                    "CASE WHEN pp.paymentId IS NULL THEN false ELSE true END AS paid " +
                    "FROM BugReports r " +
                    "JOIN Projects p ON r.projectId = p.projectId " +
                    "JOIN PaymentLevelAmounts pla ON p.projectId = pla.projectId AND r.severity = pla.level " +
                    "LEFT JOIN ProjectPayments pp ON r.reportId = pp.reportId " +
                    "WHERE r.reportId = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reportId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                reportDetails = new HashMap<>();
                reportDetails.put("reportId", rs.getInt("reportId"));
                reportDetails.put("projectId", rs.getInt("projectId"));
                reportDetails.put("hackerId", rs.getInt("hackerId"));
                reportDetails.put("severity", rs.getString("severity"));
                reportDetails.put("vulnerabilityType", rs.getString("vulnerabilityType"));
                reportDetails.put("title", rs.getString("title"));
                reportDetails.put("createdAt", rs.getTimestamp("createdAt").toString());
                reportDetails.put("status", rs.getString("status"));
                reportDetails.put("payment_amount", rs.getDouble("payment_amount"));
                reportDetails.put("paid", rs.getBoolean("paid"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching report payment details: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }

        return reportDetails;
    }

    public void recordPayment(int reportId, int hackerId, double amount) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO ProjectPayments (reportId, hackerId, amount, paymentDate) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, reportId);
            stmt.setInt(2, hackerId);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();

            conn.commit();
            logger.info("Recorded payment for report ID: " + reportId);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error rolling back transaction: {0}", ex.getMessage());
                }
            }
            logger.log(Level.SEVERE, "Error recording payment: {0}", e.getMessage());
            throw e;
        } finally {
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { }
            }
        }
    }

    public Map<String, Object> getProjectFinanceDetails(int projectId) throws SQLException {
        Map<String, Object> projectDetails = new HashMap<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConfig.getConnection();
            String sql = "SELECT projectId, totalExpenditure FROM ProjectConfigs WHERE projectId = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, projectId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                projectDetails.put("projectId", rs.getInt("projectId"));
                projectDetails.put("totalExpenditure", rs.getDouble("totalExpenditure"));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching project finance details: {0}", e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }

        return projectDetails;
    }
} 