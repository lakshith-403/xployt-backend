package com.xployt.dao.common;

import com.xployt.model.Project;
import com.xployt.model.ProjectBrief;
import com.xployt.util.ConnectionManager;
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
    private final Logger logger = CustomLogger.getLogger();

    // public static void setServletContext(ServletContext context) {
    // servletContext = context;
    // }

    public List<ProjectBrief> getAllProjects(String userId) {
        logger.info("ProjectDAO: Inside getAllProjects");
        List<ProjectBrief> projects = new ArrayList<>();
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
            logger.info("ProjectDAO: Projects fetched successfully");
            logger.info("ProjectDAO: Number of projects fetched: " + projects.size());
        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching projects: " + e.getMessage());
        }
        return projects;
    }

    public List<ProjectBrief> getAllProjectsTest(String userId) {
        logger.info("ProjectDAO: Inside getAllProjects");
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        try {
            return ConnectionManager.executeWithRetry(servletContext, conn -> {
                List<ProjectBrief> projects = new ArrayList<>();
                String sql = "SELECT * FROM Projects WHERE clientId = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, userId);
                    ResultSet rs = stmt.executeQuery();
                    logger.info("ProjectDAO: Fetching projects for user");
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
                    logger.info("ProjectDAO: Projects fetched successfully");
                    logger.info("ProjectDAO: Number of projects fetched: " + projects.size());
                }
                return projects;
            });
        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching projects: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Project getProjectById(String projectId) throws SQLException {
        logger.info("ProjectDAO: Inside getProject");
        Project project = null;
        String sql = "SELECT * FROM Projects WHERE projectId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectDAO: Connection established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                project = new Project();
                project.setProjectId(rs.getString("projectId"));
                project.setState(Project.State.valueOf(rs.getString("state")));
                project.setClientId(rs.getString("clientId"));
                project.setLeadId(rs.getString("leadId"));
                project.setTitle(rs.getString("title"));
                project.setDescription(rs.getString("description"));
                project.setStartDate(rs.getString("startDate"));
                project.setEndDate(rs.getString("endDate"));
                project.setUrl(rs.getString("url"));
                project.setTechnicalStack(rs.getString("technicalStack"));
                project.setScope(getProjectScope(projectId));
            }
            logger.info("ProjectDAO: Project fetched successfully");
        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching project: " + e.getMessage());
            throw e;
        }
        return project;
    }

    private String[] getProjectScope(String projectId){
        logger.info("ProjectDAO: Inside getScope");
        String[] scope = null;
        String sql = "SELECT scopeItems.description FROM scopeItems " +
                "INNER JOIN ProjectScope ON scopeItems.scopeId = ProjectScope.scopeId " +
                "WHERE ProjectScope.projectId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectDAO: Connection established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();
            logger.info("ProjectDAO: Fetching scope");
            if (rs.next()) {
                scope = rs.getString("description").split(",");
            }
            logger.info("ProjectDAO: Scope fetched successfully");
        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching scope: " + e.getMessage());
        }
        return scope;
    }

    public void updateProjectState(String projectId, String state) throws SQLException {
        String sql = "UPDATE Projects SET state = ? WHERE projectId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("ProjectDAO: Connection established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, state);
            stmt.setString(2, projectId);
            stmt.executeUpdate();
        }
    }

    public ArrayList<String[]> getProjectSeverityLevels(int projectId){
        String sql = "SELECT item, level FROM PaymentLevels WHERE projectId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        ArrayList<String[]> severityLevels = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String item = rs.getString("item");
                String level = rs.getString("level");
                severityLevels.add(new String[]{item, level});
            }

        } catch (SQLException e) {
            logger.severe("ProjectDAO: Error fetching severity levels: " + e.getMessage());
        }

        return severityLevels;
    }
}