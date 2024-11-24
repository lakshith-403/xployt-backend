package com.xployt.dao.lead;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.ProjectConfigInfo;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ProjectConfigInfoDAO {
    private Logger logger = CustomLogger.getLogger();

    public ProjectConfigInfo getProjectInfo(String projectId) throws SQLException {
        logger.info("ProjectDAO: Inside getProjectInfo");

        String sql = "SELECT p.*, u.*, pr.username \r\n" +
                "FROM Projects p \r\n" +
                "INNER JOIN Users u ON p.clientId = u.userId \r\n" +
                "INNER JOIN UserProfiles pr ON u.userId = pr.userId \r\n" +
                "WHERE p.projectId = ?;";

        ProjectConfigInfo project = null;

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    project = new ProjectConfigInfo();
                    project.setTitle(rs.getString("title"));
                    project.setEndDate(rs.getString("endDate"));
                    project.setStartDate(rs.getString("startDate"));
                    project.setTechnicalStack(rs.getString("technicalStack"));
                    project.setDescription(rs.getString("description"));
                    project.setUrl(rs.getString("url"));
                    project.setStatus(rs.getString("status"));

                    project.setClientId(rs.getString("clientId"));
                    project.setClientName(rs.getString("name"));
                    project.setClientEmail(rs.getString("email"));
                    project.setClientUsername(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL Error in getProjectInfo: " + e.getMessage());
        }
        return project;
    }
}