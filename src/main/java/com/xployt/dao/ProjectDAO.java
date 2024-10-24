package com.xployt.dao;

import com.xployt.model.Project;
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

public class ProjectDAO {
    // private static ServletContext servletContext;
    private Logger logger = CustomLogger.getLogger();

    // public static void setServletContext(ServletContext context) {
    // servletContext = context;
    // }

    public List<Project> getAllProjects(String userId) {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM test_project WHERE user_id = ?"; // Assuming a user_id column

        // Access the specific ServletContext by its name
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("DAO: Fetching projects for user");
            while (rs.next()) {
                Project project = new Project(
                        rs.getInt("id"),
                        rs.getString("status"),
                        rs.getString("title"),
                        rs.getString("client"),
                        rs.getInt("pending_reports"));
                projects.add(project);
            }
            logger.info("DAO: Projects fetched successfully");
            logger.info("DAO: Number of projects fetched: " + projects.size());
        } catch (SQLException e) {
            logger.severe("DAO: Error fetching projects: " + e.getMessage());
        }
        return projects;
    }
}