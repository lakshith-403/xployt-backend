package com.xployt.dao.common;

import com.xployt.model.PublicUser;
import com.xployt.model.User;
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

    // public List<String> assignValidatorsToProject(String projectId, int validatorCount) throws SQLException {
    //     List<String> assignedValidators = new ArrayList<>();

    //     ServletContext servletContext = ContextManager.getContext("DBConnection");
    //     Connection conn = (Connection) servletContext.getAttribute("DBConnection");

    //     try {
    //         conn.setAutoCommit(false);

    //         // Find validators with minimum project assignments
    //         String findValidatorsSQL = "SELECT v.userId, COUNT(pv.validatorId) as project_count " +
    //                 "FROM Users v " +
    //                 "LEFT JOIN ProjectValidators pv ON v.userId = pv.validatorId " +
    //                 "WHERE v.role = 'VALIDATOR' " +
    //                 "GROUP BY v.userId " +
    //                 "ORDER BY project_count ASC " +
    //                 "LIMIT ?";

    //         try (PreparedStatement stmt = conn.prepareStatement(findValidatorsSQL)) {
    //             stmt.setInt(1, validatorCount);
    //             ResultSet rs = stmt.executeQuery();

    //             String insertSQL = "INSERT INTO ProjectValidators (projectId, validatorId) VALUES (?, ?)";

    //             PreparedStatement insertStmt = conn.prepareStatement(insertSQL);

    //             while (rs.next()) {
    //                 String validatorId = rs.getString("userId");
    //                 insertStmt.setString(1, projectId);
    //                 insertStmt.setString(2, validatorId);
    //                 insertStmt.executeUpdate();
    //                 assignedValidators.add(validatorId);
    //             }
    //         }

    //         conn.commit();
    //         conn.close();
    //         return assignedValidators;

    //     } catch (SQLException e) {
    //         logger.severe("Error assigning validators: " + e.getMessage());
    //         conn.rollback();
    //         throw e;
    //     } finally {
    //         conn.setAutoCommit(true);
    //     }
    // }

    public List<PublicUser> getAssignedValidator(String projectId, String hackerId) {
        UserDAO userDAO = new UserDAO();
        logger.info("ProjectTeamAssignmentDAO: fetching assigned validator for hacker " + hackerId);
        List<PublicUser> validators = new ArrayList<>();
        String sql = "SELECT assignedValidatorId FROM ProjectHackers " +
                "WHERE projectId = ? AND hackerId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            stmt.setString(2, hackerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String validatorId = rs.getString("assignedValidatorId");
                User user = userDAO.getUserById(validatorId);
                PublicUser validator = new PublicUser(user.getUserId(), user.getName(), user.getEmail());
                logger.info("ProjectTeamAssignmentDAO: Assigned validator fetched successfully" + validator);
                validators.add(validator);
            }
        }catch (SQLException e) {
            logger.severe("ProjectTeamAssignmentDAO: Error fetching assigned validator: " + e.getMessage());
            return null;
        }
        return validators;
    }

    public List<PublicUser> getAssignedHacker(String projectId, String validatorId){
        UserDAO userDAO = new UserDAO();
        logger.info("ProjectDAO: fetching assigned hacker for validator " + validatorId);
        List<PublicUser> hackers =  new ArrayList<>();
        String sql = "SELECT hackerId FROM ProjectHackers " +
                "WHERE projectId = ? AND assignedValidatorId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            stmt.setString(2, validatorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String hackerId = rs.getString("hackerId");
                User user = userDAO.getUserById(hackerId);
                PublicUser hacker = new PublicUser(user.getUserId(), user.getName(), user.getEmail());
                hackers.add(hacker);
                logger.info("ProjectDAO: Assigned validator fetched successfully:" +  hackers);
            }
        }catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching assigned validator: " + e.getMessage());
            return null;
        }
        return hackers;
    }
}