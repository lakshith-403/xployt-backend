package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

import com.xployt.model.ProjectBrief;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class UserProjectsDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public List<ProjectBrief> getAllProjects(String userId, String userStatus) {
        logger.info("UserProjectsDAO: Inside getAllProjects");
        List<ProjectBrief> projects = new ArrayList<>();
        String sql;

        if (Objects.equals(userStatus, "client")) {
            sql = "SELECT * FROM Projects WHERE clientId = ?";
        } else if (Objects.equals(userStatus, "lead")) {
            sql = "SELECT * FROM Projects WHERE leadId = ?";
        } else if (Objects.equals(userStatus, "validator")) {
            sql = "SELECT DISTINCT Projects.* FROM ProjectHackers " +
                    "INNER JOIN Projects " +
                    "ON ProjectHackers.projectId = Projects.projectId " +
                    "WHERE ProjectHackers.assignedValidatorId = ?";
        } else {
            sql = "SELECT * FROM ProjectHackers " +
                    "INNER JOIN Projects " +
                    "ON ProjectHackers.projectId = Projects.projectId " +
                    "WHERE ProjectHackers.hackerId = ?";
        }

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        if (conn == null) {
            logger.severe("UserProjectsDAO: Database connection is null");
            return projects;
        }
        logger.info("UserProjectsDAO: Connection established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("UserProjectsDAO: Fetching projects for user");
            while (rs.next()) {
                ProjectBrief project = new ProjectBrief(
                        rs.getInt("projectId"),
                        rs.getString("state"),
                        rs.getString("title"),
                        rs.getString("leadId"),
                        rs.getString("clientId"),
                        rs.getString("startDate"),
                        rs.getString("endDate"),
                        rs.getInt("pendingReports"));
                projects.add(project);
            }
            logger.info("UserProjectsDAO: Projects fetched successfully");
            logger.info("UserProjectsDAO: Number of projects fetched: " + projects.size());
        } catch (SQLException e) {
            logger.severe("UserProjectsDAO: Error fetching projects: " + e.getMessage());
        }
        return projects;
    }
}
