package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class BlastPointsDAO {
    private final Logger logger = CustomLogger.getLogger();

    public int getBlastPoints(String category, String action) throws SQLException {
        logger.info("BlastPointsDAO: executing getBlastPoints");
        int blastPoints = 0;
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
                    blastPoints = rs.getInt("points");
                }
                if(blastPoints > 0) {
                    logger.info("BlastPointsDAO: Blast points fetched Successfully");
                } else {
                    logger.warning("BlastPointsDAO: No blast points found for category " + category + " and action " + action);
                }
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error fetching blast points" + e.getMessage());
            throw e;
        }

        return blastPoints;
    }

    public int getUserBlastPoints(String userId) {
        logger.info("BlastPointsDAO: executing getUserBlastPoints");
        int blastPoints = 0;

        String sql = "SELECT points FROM HackerBlastPoints WHERE userId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("BlastPointsDAO: Fetching blast points for user " + userId);
            if (rs.next()) {
                blastPoints = rs.getInt("points");
            }
            System.out.println("BlastPoints: " + blastPoints);
            logger.info("BlastPointsDAO: Blast points fetched Successfully");
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error fetching blast points" + e.getMessage());
        }

        return blastPoints;
    }

    public void addNewUserBlastPoints(int userId) throws SQLException {
        logger.info("BlastPointsDAO: executing addNewUserBlastPoints");
        String sql = "INSERT INTO HackerBlastPoints (userId, points) VALUES (?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, 0);
            stmt.executeUpdate();
            logger.info("BlastPointsDAO: New user blast points added successfully");
            addUserBlastPoints(userId, "profile", "sign_up");
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error adding new user blast points " + e.getMessage());
            throw e;
        }
    }

    public void addUserBlastPoints(int userId, String category, String action) throws SQLException {
        logger.info("BlastPointsDAO: executing addUserBlastPoints");
        int blastPoints = getBlastPoints(category, action);

        String checkUserSql = "SELECT userId FROM HackerBlastPoints WHERE userId = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                addNewUserBlastPoints(userId);
            }
        }

        String sql = "UPDATE HackerBlastPoints SET points = points + ? WHERE userId = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, blastPoints);
            stmt.setInt(2, userId);
            int affectedRows = stmt.executeUpdate();
            if(affectedRows > 0) {
                logger.info("BlastPointsDAO: User blast points updated successfully");
            } else {
                throw new SQLException("BlastPointsDAO: User not found");
            }
        } catch (SQLException e) {
            logger.severe("BlastPointsDAO: Error adding blast points " + e.getMessage());
            throw e;
        }
    }
}
