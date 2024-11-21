package com.xployt.dao;

import com.xployt.model.Project;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectDAO {
    // private static ServletContext servletContext;
    private Logger logger = CustomLogger.getLogger();

    // public static void setServletContext(ServletContext context) {
    // servletContext = context;
    // }

    public List<Project> getAllProjects(String userId) {
        logger.info("ProjectDAO: Inside getAllProjects");
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM Projects WHERE clientId = ?"; // Assuming a user_id column

        // Access the specific ServletContext by its name
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectDAO: Connection established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("ProjectDAO: Fetching projects for user");
            while (rs.next()) {
                Project project = new Project(
                        rs.getInt("projectId"),
                        rs.getString("status"),
                        rs.getString("title"),
                        rs.getString("clientId"),
                        rs.getInt("pendingReports"));
                projects.add(project);
            }
            logger.info("ProjectDAO: Projects fetched successfully");
            logger.info("ProjectDAO: Number of projects fetched: " + projects.size());
        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching projects: " + e.getMessage());
        }
        return projects;
    }
}