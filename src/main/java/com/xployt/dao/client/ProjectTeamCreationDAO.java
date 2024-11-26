package com.xployt.dao.client;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectTeamCreationDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public void createProject(String projectId, String clientId, String projectLeadId) throws SQLException {
        String sql = "INSERT INTO Projects (projectId, clientId, leadId, status) VALUES (?, ?, ?, 'PENDING')";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            stmt.setString(2, clientId);
            stmt.setString(3, projectLeadId);
            stmt.executeUpdate();
            logger.info("Project created successfully");
        } catch (SQLException e) {
            logger.severe("Error creating project: " + e.getMessage());
            throw e;
        }
    }

    public void assignValidators(String projectId, int numberOfValidators) throws SQLException {
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        // Get eligible validators (those with less than 3 projects)
        String validatorQuery = 
            "SELECT v.validatorId, COUNT(pv.projectId) as projectCount " +
            "FROM ValidatorInfo v " +
            "LEFT JOIN ProjectValidators pv ON v.validatorId = pv.validatorId " +
            "WHERE v.approved = 1 " +
            "GROUP BY v.validatorId " +
            "HAVING COUNT(pv.projectId) < 3 " +
            "ORDER BY projectCount ASC " +
            "LIMIT ?";

        List<String> eligibleValidators = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(validatorQuery)) {
            stmt.setInt(1, numberOfValidators);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                eligibleValidators.add(rs.getString("validatorId"));
            }
        }

        // Assign validators to project
        String assignQuery = "INSERT INTO ProjectValidators (projectId, validatorId) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(assignQuery)) {
            for (String validatorId : eligibleValidators) {
                stmt.setString(1, projectId);
                stmt.setString(2, validatorId);
                stmt.addBatch();
            }
            stmt.executeBatch();
            logger.info("Validators assigned successfully");
        } catch (SQLException e) {
            logger.severe("Error assigning validators: " + e.getMessage());
            throw e;
        }
    }
} 