package com.xployt.service.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.xployt.dao.common.BlastPointsDAO;
import com.xployt.dao.common.InvitationDAO;
import com.xployt.dao.hacker.HackerDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Hacker;
import com.xployt.model.Invitation;
import com.xployt.util.CustomLogger;


public class InvitationService {
    private final InvitationDAO invitationDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public InvitationService() {
        this.invitationDAO = new InvitationDAO();
    }


    public GenericResponse fetchHackerInvitations(String userId) throws SQLException {
        logger.info("InvitationService: Fetching invitations of user " + userId);
        List<Invitation> invitations = invitationDAO.getHackerInvitations(userId);
        if (!invitations.isEmpty()) {
            return new GenericResponse(invitations, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to load invitations", null);
    }

    public GenericResponse fetchProjectInvitations(String projectId) throws SQLException {
        logger.info("InvitationService: Fetching invitations of project " + projectId);
        List<Invitation> invitations = invitationDAO.getProjectInvitations(projectId);
        if (!invitations.isEmpty()) {
            return new GenericResponse(invitations, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to load invitations", null);
    }

    public GenericResponse fetchInvitedHackers(String projectId) throws SQLException {
        logger.info("InvitationsService: Fetching invited hackers for project " + projectId);
        List<Invitation> invitations = invitationDAO.getProjectInvitations(projectId);
        List<Hacker> hackers = new ArrayList<>();

        if (invitations.isEmpty()) {
            logger.info("InvitationsService: No invitations sent yet.");
            return new GenericResponse(hackers, true, "No invitations sent", null);
        }

        HackerDAO hackerDAO = new HackerDAO();
        for (Invitation invitation : invitations) {
            Hacker hacker = hackerDAO.getHackerById(String.valueOf(invitation.getHackerId()));
            if (hacker != null) {
                hackers.add(hacker);
            }
        }

        if (!hackers.isEmpty()) {
            logger.info("No of Hackers invited " + hackers.size());
            return new GenericResponse(hackers, true, null, null);
        }

        return new GenericResponse(null, false, "Failed to load hackers", null);
    }

    public GenericResponse createInvitation(Invitation invitation) throws SQLException {
        logger.info("InvitationService: Creating invitation for user " + invitation.getHackerId());
        Invitation createdInvitation = invitationDAO.createInvitation(invitation);
        if (createdInvitation != null) {
            return new GenericResponse(invitation, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to create invitation", null);
    }

    public GenericResponse acceptInvitation(Invitation invitation) throws SQLException {
        logger.info("InvitationService: Accepting invitation for user " + invitation.getHackerId());
        Invitation acceptedInvitation = invitationDAO.acceptInvitation(invitation);
        BlastPointsDAO blastPointsDAO = new BlastPointsDAO();
        blastPointsDAO.addUserBlastPoints(invitation.getHackerId(), "participation", "project_participation");
        
        if (acceptedInvitation != null) {
            logger.info("InvitationService: Success");
            
            return new GenericResponse(invitation, true, null, null);
        }
        
        logger.info("InvitationService: Failed");
        return new GenericResponse(null, false, "Failed to accept invitation", null);
    }

    public GenericResponse rejectInvitation(Invitation invitation) throws SQLException {
        logger.info("InvitationService: Rejecting invitation for user " + invitation.getHackerId());
        Invitation rejectedInvitation = invitationDAO.rejectInvitation(invitation);
        if (rejectedInvitation != null) {
            return new GenericResponse(invitation, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to reject invitation", null);
    }
}