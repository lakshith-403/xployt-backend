package com.xployt.dao.client;

import com.xployt.dao.common.BlastPointsDAO;
import com.xployt.model.Hacker;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class HackerFilterDAO {
    private final Logger logger = CustomLogger.getLogger();

    public List<String> getAllHackerIds() throws SQLException {
        String sql = "SELECT userId FROM Users WHERE role = 'Hacker'";
        List<String> hackerIds = new ArrayList<>();
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                hackerIds.add(rs.getString("userId"));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker IDs: " + e.getMessage());
            throw e;
        }

        return hackerIds;
    }

    public Set<String> getProjectSkills(String projectId) throws SQLException {
        String sql = "SELECT scopeItems.scopeName FROM scopeItems " +
                "INNER JOIN ProjectScope ON scopeItems.scopeId = ProjectScope.scopeId " +
                "WHERE ProjectScope.projectId = ?";
        Set<String> skills = new HashSet<>();
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(rs.getString("scopeName"));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching project skills: " + e.getMessage());
            throw e;
        }

        return skills;
    }

    public Set<String> getHackerSkills(String hackerId) throws SQLException {
        String sql = "SELECT HackerSkillSet.skill FROM HackerSkillSet " +
                "WHERE HackerSkillSet.hackerId = ?";
        Set<String> skills = new HashSet<>();
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                skills.add(rs.getString("skill"));
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker skills: " + e.getMessage());
            throw e;
        }

        return skills;
    }

    public Hacker getHackerById(String hackerId) throws SQLException {
        BlastPointsDAO blastPointsDAO = new BlastPointsDAO();
        String sql = "SELECT Users.userId, Users.name, Users.email FROM Users WHERE userId = ?";
        Hacker hacker = null;
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int blastPoints = blastPointsDAO.getUserBlastPoints(hackerId);
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hacker = new Hacker(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        blastPoints
                );
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker by ID: " + e.getMessage());
            throw e;
        }

        return hacker;
    }
}