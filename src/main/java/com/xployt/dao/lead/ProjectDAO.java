package com.xployt.dao.lead;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;
import com.xployt.model.Project;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class ProjectDAO {
    private Logger logger = CustomLogger.getLogger();

    public Project getProjectInfo(String projectId) {
        logger.info("ProjectDAO: Inside getProjectInfo");

        String sql = "SELECT * FROM Projects WHERE projectId = ? LIMIT 1";
        Project project = null;

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try (Connection conn = (Connection) servletContext.getAttribute("DBConnection");
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    project = new Project();
                    project.setTitle(rs.getString("title"));
                    project.setEndDate(rs.getString("endDate"));
                    project.setStartDate(rs.getString("startDate"));

                    // Set other fields as necessary
                }
            }
        } catch (SQLException e) {
            logger.severe("SQL Error in getProjectInfo: " + e.getMessage());
        }
        return project;
    }
}