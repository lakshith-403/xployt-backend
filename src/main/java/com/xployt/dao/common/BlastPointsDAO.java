package com.xployt.dao.common;

import com.xployt.model.BlastPoints;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class BlastPointsDAO {
    private final Logger logger = CustomLogger.getLogger();

    public BlastPoints getBlastPoints(String category, String action) throws SQLException {
        logger.info("BlastPointsDAO: executing getBlastPoints");
        BlastPoints blastPoints;
        String sql = "SELECT * FROM PointsConfig WHERE category = ? && action = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("BlastPointsDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, action);
            ResultSet rs = stmt.executeQuery();
            logger.info("BlastPointsDAO: Fetching blast points for category " + category + " and action " + action);

                if(rs.next()) {
                    blastPoints = new BlastPoints(
                            rs.getInt("points")
                    );
                } else {
                    blastPoints = new BlastPoints(0);
                }
            logger.info("BlastPointsDAO: Blast points fetched Successfully");
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error fetching blast points" + e.getMessage());
            throw e;
        }

        return blastPoints;
    }

    public BlastPoints getUserBlastPoints(String userId) {
        logger.info("BlastPointsDAO: executing getUserBlastPoints");
        BlastPoints blastPoints = new BlastPoints();

        String sql = "SELECT points FROM HackerBlastPoints WHERE hackerId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("BlastPointsDAO: Fetching blast points for user " + userId);
            if (rs.next()) {
                blastPoints = new BlastPoints(
                        rs.getInt("points")
                );
            }
            logger.info("BlastPointsDAO: Blast points fetched Successfully");
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error fetching blast points" + e.getMessage());
        }

        return blastPoints;
    }

    public void addUserBlastPoints(int userID, String category, String action) throws SQLException {
        logger.info("BlastPointsDAO: executing addUserBlastPoints");
        BlastPoints blastPoints = getBlastPoints(category, action);

        String sql = "UPDATE HackerBlastPoints SET points = points + ? WHERE userId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, blastPoints.getPoints());
            stmt.setInt(2, userID);
            stmt.executeUpdate();
            logger.info("BlastPointsDAO: Blast points added Successfully");
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error adding blast points " + e.getMessage());
            throw e;
        }
    }
}
