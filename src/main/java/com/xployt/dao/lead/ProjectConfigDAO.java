package com.xployt.dao.lead;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.ProjectConfig;

public class ProjectConfigDAO {
    private Logger logger = CustomLogger.getLogger();

    public void updateProjectConfig(ProjectConfig projectConfig) {
        logger.info("ProjectConfigDAO updateProjectConfig method called");
        String sql = "INSERT INTO ProjectConfigs (critical, high, medium, low, informative, visibility, initialFunding, attachments, testingScope, outOfScope, objectives, securityRequirements, projectId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectConfig.getCritical());
            stmt.setString(2, projectConfig.getHigh());
            stmt.setString(3, projectConfig.getMedium());
            stmt.setString(4, projectConfig.getLow());
            stmt.setString(5, projectConfig.getInformative());
            stmt.setString(6, projectConfig.getVisibility());
            stmt.setString(7, projectConfig.getInitialFunding());
            stmt.setString(8, projectConfig.getAttachments());
            stmt.setString(9, projectConfig.getTestingScope());
            stmt.setString(10, projectConfig.getOutOfScope());
            stmt.setString(11, projectConfig.getObjectives());
            stmt.setString(12, projectConfig.getSecurityRequirements());
            stmt.setString(13, projectConfig.getProjectId());
            stmt.executeUpdate();
            logger.info("ProjectConfigDAO updateProjectConfig method completed");
        } catch (SQLException e) {
            logger.severe("SQL Error in getProjectInfo: " + e.getMessage());
        }
    }
}
