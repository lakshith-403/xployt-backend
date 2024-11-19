package com.xployt.dao;

import com.xployt.model.Project;
import com.xployt.model.ProjectHacker;
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

public class projectHackerDAO {
    private final Logger logger = CustomLogger.getLogger();

    public List<ProjectHacker> getProjectHackers() {
        logger.info("ProjectHackerDAO: executing getAllHackers");
        List<ProjectHacker> projectHackers = new ArrayList<>();
        String sql = "SELECT * FROM ProjectHacker";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectHackerDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, null);
            ResultSet rs = stmt.executeQuery();
            logger.info("ProjectHackerDAO: Fetching hackers of a project");
            while (rs.next()) {
                ProjectHacker projectHacker = new ProjectHacker(
                        rs.getInt("projectHackerId"),
                        rs.getInt("projectId"),
                        rs.getInt("hackerId"),
                        rs.getInt("assignedValidatorId"),
                        rs.getString("status"),
                        rs.getString("timestamp")
                );
                projectHackers.add(projectHacker);
            }
            logger.info("ProjectHackerDAO: Hackers of a project fetched Successfully");
            logger.info("ProjectHackerDAO: Number of hackers fetched " + projectHackers.size());
        } catch (SQLException e) {
            logger.severe("ProjectHackerDAO: Error fetching hackers" + e.getMessage());
        }

        return projectHackers;
    }
}
