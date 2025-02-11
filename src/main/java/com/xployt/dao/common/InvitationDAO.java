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

    public List<Invitation> getHackerInvitations(String userId) throws SQLException {
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
//                if(rs.getString("status").equals("Pending")) {
                    Invitation invitation = new Invitation(
                            rs.getInt("HackerID"),
                            rs.getInt("ProjectID"),
                            rs.getString("Status"),
                            rs.getString("InvitedAt")
                    );
                    invitations.add(invitation);
//                }
            }
            logger.info("InvitationDAO: Hackers of a project fetched Successfully");
            logger.info("InvitationDAO: Number of invitations fetched " + invitations.size());
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error fetching hackers" + e.getMessage());
            throw e;
        }

        return invitations;
    }

    public Invitation createInvitation(Invitation invitation) throws SQLException{
        logger.info("InvitationDAO: creating invitation");

        String sql = "INSERT INTO Invitations (HackerID, ProjectID) VALUES (?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, invitation.getHackerId());
            stmt.setInt(2, invitation.getProjectId());
//            stmt.setString(3, invitation.getStatus());
            stmt.executeUpdate();
            logger.info("InvitationDAO: Invitation created successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error creating invitation" + e.getMessage());
            throw e;
        }

        return invitation;
    }

    public Invitation acceptInvitation(Invitation invitation) throws SQLException{
        logger.info("InvitationDAO: accepting invitation");

        String sql = "UPDATE Invitations SET Status = 'Accepted' WHERE HackerID = ? AND ProjectID = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, invitation.getHackerId());
            stmt.setInt(2, invitation.getProjectId());
            stmt.executeUpdate();
            logger.info("InvitationDAO: Invitation accepted successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error accepting invitation" + e.getMessage());
            throw e;
        }

        addProjectHacker(invitation);

        return invitation;
    }

    public Invitation rejectInvitation(Invitation invitation) throws SQLException{
        logger.info("InvitationDAO: rejecting invitation");

        String sql = "UPDATE Invitations SET Status = 'Declined' WHERE HackerID = ? AND ProjectID = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, invitation.getHackerId());
            stmt.setInt(2, invitation.getProjectId());
            stmt.executeUpdate();
            logger.info("InvitationDAO: Invitation rejected successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error rejecting invitation" + e.getMessage());
            throw e;
        }

        return invitation;
    }

    public void addProjectHacker(Invitation invitation) throws SQLException {
        logger.info("Adding Hacker to project " + invitation.getProjectId());

        String sql = "INSERT INTO ProjectHackers (projectId, hackerId) VALUES (?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(2, invitation.getHackerId());
            stmt.setInt(1, invitation.getProjectId());
            stmt.executeUpdate();
            logger.info("InvitationDAO: Hacker added successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error adding hacker" + e.getMessage());
            throw e;
        }
    }
}