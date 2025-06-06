package com.xployt.dao.common;

import com.xployt.model.Invitation;
import com.xployt.model.ProjectTeam;
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
    private final BlastPointsDAO blastPointsDAO = new BlastPointsDAO();

    public List<Invitation> getHackerInvitations(String userId) throws SQLException {
        logger.info("InvitationDAO: executing getHackerInvitations");
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM Invitations WHERE HackerID = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("InvitationDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(userId));
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
            logger.info("InvitationDAO: Invitations of hacker fetched Successfully");
            logger.info("InvitationDAO: Number of invitations fetched " + invitations.size());
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error fetching hacker invitations" + e.getMessage());
            throw e;
        }

        return invitations;
    }

    public List<Invitation> getProjectInvitations(String projectId, Boolean filter) throws SQLException {
        logger.info("InvitationDAO: executing getProjectInvitations");
        List<Invitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM Invitations WHERE ProjectID = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        logger.info("InvitationDAO: Connection Established");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(projectId));
            logger.info(stmt.toString());
            ResultSet rs = stmt.executeQuery();
            logger.info("InvitationDAO: Fetching project invitations for " + projectId);
            while (rs.next()) {
                Invitation invitation = new Invitation(
                        rs.getInt("HackerID"),
                        rs.getInt("ProjectID"),
                        rs.getString("Status"),
                        rs.getString("InvitedAt")
                );
                invitations.add(invitation);
            }
            if(filter){
                invitations.removeIf(invitation -> invitation.getStatus().equals("Accepted") || invitation.getStatus().equals("Rejected"));
            }
            logger.info("InvitationDAO: Number of project invitations fetched " + invitations.size());
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error fetching project invitations" + e.getMessage());
            throw e;
        }

        return invitations;
    }

    public Invitation createInvitation(Invitation invitation) throws SQLException {
        logger.info("InvitationDAO: creating invitation");

        String sql = "INSERT INTO Invitations (HackerID, ProjectID, Status) VALUES (?, ?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, invitation.getHackerId());
            stmt.setInt(2, invitation.getProjectId());
            stmt.setString(3, "Pending");
            stmt.executeUpdate();

            NotificationDAO notificationDAO = new NotificationDAO();
            notificationDAO.createNotification(
                    String.valueOf(invitation.getHackerId()),
                    "New Invitation",
                    "You have been invited to join the project " + invitation.getProjectId(),
                    "/dashboard"
            );
            logger.info("InvitationDAO: Invitation created successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error creating invitation" + e.getMessage());
            throw e;
        }

        return invitation;
    }

    public Invitation acceptInvitation(Invitation invitation) throws SQLException {
        logger.info("InvitationDAO: accepting invitation");

        String sql = "UPDATE Invitations SET Status = 'Accepted' WHERE HackerID = ? AND ProjectID = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, invitation.getHackerId());
            stmt.setInt(2, invitation.getProjectId());
            stmt.executeUpdate();

            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(String.valueOf(invitation.getProjectId()));
            NotificationDAO notificationDAO = new NotificationDAO();
            notificationDAO.sendNotificationToMultipleUsers(
                    List.of(projectTeam.getClient(), projectTeam.getProjectLead()),
                    "Invitation - #" + invitation.getProjectId(),
                    "Hacker " + invitation.getHackerId() + " has accepted the invitation to join the project ",
                    "/projects/" + invitation.getProjectId()
            );

            logger.info("InvitationDAO: Invitation accepted successfully");
        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error accepting invitation" + e.getMessage());
            throw e;
        }

        addProjectHacker(invitation);

        return invitation;
    }

    public Invitation rejectInvitation(Invitation invitation) throws SQLException {
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

            blastPointsDAO.addUserBlastPoints(invitation.getHackerId(), "participation", "project_participation");

        } catch (SQLException e) {
            logger.severe("InvitationDAO: Error adding hacker" + e.getMessage());
            throw e;
        }
    }
}