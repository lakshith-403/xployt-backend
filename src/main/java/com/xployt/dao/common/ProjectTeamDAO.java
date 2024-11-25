package com.xployt.dao.common;

import com.xployt.model.Project;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
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

public class ProjectTeamDAO {
    private final Logger logger = CustomLogger.getLogger();

    public ProjectTeam getProjectTeam(String projectId) throws SQLException {
        logger.info("ProjectTeamDAO: executing getProjectTeam");
        ProjectTeam projectTeam = new ProjectTeam();
        Project project = new Project();

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectTeamDAO: Connection Established");

        ProjectDAO projectDAO = new ProjectDAO();
        project = projectDAO.getProject(projectId);

        projectTeam.setProjectId(projectId);
        projectTeam.setClient(retrieveAndMapUser(conn, project.getClientId()));
        projectTeam.setProjectLead(retrieveAndMapUser(conn, project.getProjectLeadId()));
        projectTeam.setProjectHackers(getProjectHackers(conn, projectId));
        projectTeam.setProjectValidators(getProjectValidators(conn, projectId));

        return projectTeam;
    }

    private PublicUser retrieveAndMapUser(Connection conn, String userId) throws SQLException {
        String sql = "SELECT * FROM Users WHERE userId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new PublicUser(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            logger.severe("ProjectTeamDAO: Error retrieving user data: " + e.getMessage());
            throw new SQLException("Error retrieving user data", e);
        }
        return null;
    }

    private List<PublicUser> getProjectHackers(Connection conn, String projectId) throws SQLException {
        List<PublicUser> hackers = new ArrayList<>();
        String sql = "SELECT hackerId FROM ProjectHackers WHERE projectId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String hackerId = rs.getString("hackerId");
                PublicUser hacker = retrieveAndMapUser(conn, hackerId);
                if (hacker != null) {
                    hackers.add(hacker);
                }
            }
        } catch (SQLException e) {
            logger.severe("ProjectTeamDAO: Error retrieving hacker data: " + e.getMessage());
            throw new SQLException("Error retrieving hacker data", e);
        }
        return hackers;
    }

    public List<PublicUser> getProjectValidators(Connection conn, String projectId) throws SQLException {
        List<PublicUser> validators = new ArrayList<>();
        String sql = "SELECT validatorId FROM ProjectValidators WHERE projectId = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String validatorId = rs.getString("validatorId");
                PublicUser validator = retrieveAndMapUser(conn, validatorId);
                if (validator != null) {
                    validators.add(validator);
                }
            }
        } catch (SQLException e) {
            logger.severe("ProjectTeamDAO: Error retrieving validator data: " + e.getMessage());
            throw new SQLException("Error retrieving validator data", e);
        }
        return validators;
    }
}
