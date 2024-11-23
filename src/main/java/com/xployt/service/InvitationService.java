package com.xployt.service;

import com.xployt.dao.InvitationDAO;
import com.xployt.model.GenericResponse;
import com.xployt.model.Invitation;
import com.xployt.util.CustomLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class InvitationService {
    private final InvitationDAO invitationDAO;
//    private static final List<String> STATUS_FILTER = Arrays.asList("Invited");
    private static final Logger logger = CustomLogger.getLogger();

    public InvitationService() {this.invitationDAO = new InvitationDAO();}

    public GenericResponse fetchHackerInvitations(String userId) {
        logger.info("InvitationService: Fetching projectHackers" + userId);

        List<Invitation> allInvitations = invitationDAO.getHackerInvitations(userId);

//        List<Invitation> filteredInvitations = new ArrayList<>();
//        List<Invitation> remainingInvitations = new ArrayList<>();

//        for(Invitation invitation : allInvitations) {
//            if(STATUS_FILTER.contains(invitation.getStatus())) {
//                filteredInvitations.add(invitation);
//            }else{
//                remainingInvitations.add(invitation);
//            }
//        }

        logger.info("All Invitations: " + allInvitations.size());
        List<List<Invitation>> result = new ArrayList<>();
        result.add(allInvitations);

        return new GenericResponse(result, true, null, null);

    }
}