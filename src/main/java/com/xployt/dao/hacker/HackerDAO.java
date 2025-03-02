package com.xployt.dao.hacker;

import com.xployt.dao.common.BlastPointsDAO;
import com.xployt.model.Hacker;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class HackerDAO {
    private final Logger logger = CustomLogger.getLogger();
    private final BlastPointsDAO blastPointsDAO = new BlastPointsDAO();

    public Hacker getHackerById(String hackerId) throws SQLException {
        logger.info("HackerDAO: executing getHackerById");

        Hacker hacker = null;

        String sql = "SELECT * FROM Users WHERE userId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("HackerDAO: Connection Established");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            logger.info("HackerDAO: Fetching hacker by ID");
            if (rs.next()) {
                if (!Objects.equals(rs.getString("role"), "Hacker")) {
                    throw new IllegalArgumentException("User is not a hacker");
                }
                int blastPoints = blastPointsDAO.getUserBlastPoints(hackerId);
//                String[] skills = getHackerSkills(hackerId).toArray(new String[0]);
                 hacker = new Hacker(
                        String.valueOf(rs.getInt("userId")),
                        rs.getString("name"),
                        rs.getString("email"),
                        blastPoints
                );
            }
        } catch (SQLException e) {
            logger.severe("HackerDAO: Error fetching hacker" + e.getMessage());
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return hacker;
    }

    public List<String> getHackerSkills(String hackerId) throws SQLException {
        logger.info("HackerDAO: executing getHackerSkills");

        List<String> skills = new ArrayList<>();
        String sql = "SELECT skill FROM HackerSkillSet WHERE hackerId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("HackerDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hackerId);
            ResultSet rs = stmt.executeQuery();
            logger.info("HackerDAO: Fetching hacker skills");
            while (rs.next()) {
                skills.add(rs.getString("skill"));
            }
            logger.info("HackerDAO: Skills fetched Successfully");
        } catch (SQLException e) {
            logger.severe("HackerDAO: Error fetching hacker skills" + e.getMessage());
            throw e;
        }
        return skills;
    }
}
