package com.xployt.dao.common;

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

public class ProjectTeamAssignmentDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public List<String> assignValidatorsToProject(String projectId, int validatorCount) throws SQLException {
        List<String> assignedValidators = new ArrayList<>();
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try {
            conn.setAutoCommit(false);
            
            // Find validators with minimum project assignments
            String findValidatorsSQL = 
                "SELECT v.userId, COUNT(pv.validatorId) as project_count " +
                "FROM Users v " +
                "LEFT JOIN ProjectValidators pv ON v.userId = pv.validatorId " +
                "WHERE v.role = 'VALIDATOR' " +
                "GROUP BY v.userId " +
                "ORDER BY project_count ASC " +
                "LIMIT ?";

            try (PreparedStatement stmt = conn.prepareStatement(findValidatorsSQL)) {
                stmt.setInt(1, validatorCount);
                ResultSet rs = stmt.executeQuery();

                String insertSQL = 
                    "INSERT INTO ProjectValidators (projectId, validatorId) VALUES (?, ?)";
                
                PreparedStatement insertStmt = conn.prepareStatement(insertSQL);
                
                while (rs.next()) {
                    String validatorId = rs.getString("userId");
                    insertStmt.setString(1, projectId);
                    insertStmt.setString(2, validatorId);
                    insertStmt.executeUpdate();
                    assignedValidators.add(validatorId);
                }
            }

            conn.commit();
            return assignedValidators;

        } catch (SQLException e) {
            logger.severe("Error assigning validators: " + e.getMessage());
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}