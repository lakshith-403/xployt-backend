package com.xployt.dao.common;

import com.xployt.model.Invitation;
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

public class InvitationDAO {
    private final Logger logger = CustomLogger.getLogger();

    public List<Invitation> getHackerInvitations(String userId) {
        logger.info("InvitationDAO: executing getHackerInvitations");
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM Invitations WHERE hackerId = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("InvitationDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            logger.info("InvitationDAO: Fetching hacker's invitations");
            while (rs.next()) {
                Invitation invitation = new Invitation(
                        rs.getInt("hackerId"),
                        rs.getInt("projectId"),
                        rs.getString("InvitedAt")
                );
                invitations.add(invitation);
            }
            logger.info("InvitationDAO: Hackers of a project fetched Successfully");
            logger.info("InvitationDAO: Number of hackers fetched " + invitations.size());
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error fetching hackers" + e.getMessage());
        }

        return invitations;
    }
}