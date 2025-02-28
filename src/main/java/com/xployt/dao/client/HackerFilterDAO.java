package com.xployt.dao.client;

import com.xployt.model.PublicUser;
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
    private static final int MAX_SELECTED_HACKERS = 4;
    private static final double SIMILARITY_THRESHOLD = 0.1;

    public List<PublicUser> filterHackersForProject(String projectId) throws SQLException {
        System.out.println("filterHackersForProject: Start");
        List<PublicUser> matchedHackers = new ArrayList<>();
//        List<String> hackerIds = getHackerIdsMatchingBountyLevels(projectId);
        List<String> hackerIds = getAllHackers();
        Set<String> projectSkills = getProjectSkills(projectId);

        for (String hackerId : hackerIds) {
            Set<String> hackerSkills = getHackerSkills(hackerId);
            double similarity = jaccardSimilarity(hackerSkills, projectSkills);
            System.out.println("Hacker ID: " + hackerId + ", Similarity: " + similarity);
            if (similarity >= SIMILARITY_THRESHOLD) {
                PublicUser hacker = getHackerById(hackerId);
                matchedHackers.add(hacker);
            }
        }

        List<PublicUser> selectedHackers = selectHackersWithSpreadBlastPoints(matchedHackers);
        System.out.println("filterHackersForProject: End, Selected Hackers: " + selectedHackers);
        return selectedHackers;
    }

    private List<String> getAllHackers(){
        System.out.println("getAllHackers: Start");
        String sql = "SELECT userId FROM Users WHERE role = 'Hacker'";
        List<String> hackerIds = new ArrayList<>();
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hackerIds.add(rs.getString("userId"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker IDs: " + e.getMessage());
        }

        System.out.println("getAllHackers: End, Hacker IDs: " + hackerIds);
        return hackerIds;
    }

    private List<String> getHackerIdsMatchingBountyLevels(String projectId) throws SQLException {
        System.out.println("getHackerIdsMatchingBountyLevels: Start");
        String sql = "SELECT DISTINCT h.userId " +
                "FROM Users h " +
                "JOIN HackerMinPayment hp ON h.userId = hp.hackerId " +
                "JOIN PaymentLevelAmounts pb ON pb.projectId = ? " +
                "LEFT JOIN Invitations i ON h.userId = i.hackerId AND i.projectId = ? " +
                "WHERE hp.amount <= pb.amount AND i.hackerId IS NULL;";
        List<String> hackerIds = new ArrayList<>();
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            stmt.setString(2, projectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    hackerIds.add(rs.getString("userId"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker IDs: " + e.getMessage());
            throw e;
        }

        System.out.println("getHackerIdsMatchingBountyLevels: End, Hacker IDs: " + hackerIds);
        return hackerIds;
    }

    private Set<String> getProjectSkills(String projectId) throws SQLException {
        System.out.println("getProjectSkills: Start");
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

        System.out.println("getProjectSkills: End, Skills: " + skills);
        return skills;
    }

    private Set<String> getHackerSkills(String hackerId) throws SQLException {
        System.out.println("getHackerSkills: Start");
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

        System.out.println("getHackerSkills: End, Hacker ID: " + hackerId + ", Skills: " + skills);
        return skills;
    }

    private double jaccardSimilarity(Set<String> set1, Set<String> set2) {
        System.out.println("jaccardSimilarity: Start");
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        System.out.println("jaccardSimilarity: End, Similarity: " + similarity);
        return similarity;
    }

    private PublicUser getHackerById(String hackerId) throws SQLException {
        System.out.println("getHackerById: Start");
        String sql = "SELECT * FROM Users WHERE userId = ?";
        PublicUser hacker = null;
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hacker = new PublicUser(
                        rs.getString("userId"),
                        rs.getString("name"),
                        rs.getString("email")
                );
            }
        } catch (SQLException e) {
            logger.severe("Error fetching hacker by ID: " + e.getMessage());
            throw e;
        }

        System.out.println("getHackerById: End, Hacker: " + hacker);
        return hacker;
    }

    private List<PublicUser> selectHackersWithSpreadBlastPoints(List<PublicUser> hackers) throws SQLException {
        System.out.println("selectHackersWithSpreadBlastPoints: Start, All Hackers: " + hackers);
        if (hackers.size() <= MAX_SELECTED_HACKERS) {
            return hackers;
        }

        List<Integer> blastPoints = new ArrayList<>();
        for (PublicUser hacker : hackers) {
            blastPoints.add(getBlastPointsByHackerId(hacker.getUserId()));
        }

        List<PublicUser> selectedHackers = new ArrayList<>();
        int step = hackers.size() / MAX_SELECTED_HACKERS;

        for (int i = 0; i < MAX_SELECTED_HACKERS; i++) {
            selectedHackers.add(hackers.get(i * step));
        }

        // If selected hackers are less than 5, fill the remaining slots with random hackers
        Random random = new Random();
        while (selectedHackers.size() < MAX_SELECTED_HACKERS) {
            PublicUser randomHacker = hackers.get(random.nextInt(hackers.size()));
            if (!selectedHackers.contains(randomHacker)) {
                selectedHackers.add(randomHacker);
            }
        }

        System.out.println("selectHackersWithSpreadBlastPoints: End, Selected Hackers: " + selectedHackers);
        return selectedHackers;
    }

    private int getBlastPointsByHackerId(String hackerId) throws SQLException {
        System.out.println("getBlastPointsByHackerId: Start");
        String sql = "SELECT points FROM HackerBlastPoints WHERE userId = ?";
        int points = 0;
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                points = rs.getInt("points");
            }
        } catch (SQLException e) {
            logger.severe("Error fetching blast points: " + e.getMessage());
            throw e;
        }

        System.out.println("getBlastPointsByHackerId: End, Hacker ID: " + hackerId + ", Points: " + points);
        return points;
    }
}