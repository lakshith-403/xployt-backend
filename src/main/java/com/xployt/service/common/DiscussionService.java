package com.xployt.service.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.common.DiscussionDAO;
import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.util.CustomLogger;

public class DiscussionService {
    private final DiscussionDAO discussionDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public DiscussionService() {
        this.discussionDAO = new DiscussionDAO();
    }

    public GenericResponse fetchDiscussions(String projectId) throws SQLException {
        logger.log(Level.INFO, "Fetching discussions for projectId: {0}", projectId);
        List<Discussion> discussions = discussionDAO.getDiscussionsByProjectId(projectId);
        return new GenericResponse(discussions, true, null, null);
    }

    public GenericResponse createDiscussion(Discussion discussion) throws SQLException {
        logger.log(Level.INFO, "Creating discussion for project: {0}", discussion.getProjectId());
        Discussion createdDiscussion = discussionDAO.createDiscussion(discussion);
        if (createdDiscussion != null) {
            return new GenericResponse(createdDiscussion, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to create discussion", null);
    }

    public GenericResponse updateDiscussion(Discussion discussion) throws SQLException {
        logger.log(Level.INFO, "Updating discussion: {0}", discussion.getId());
        discussionDAO.updateDiscussion(discussion);
        return new GenericResponse(discussion, true, null, null);
    }

    public GenericResponse deleteDiscussion(String discussionId) throws SQLException {
        logger.log(Level.INFO, "Deleting discussion: {0}", discussionId);
        discussionDAO.deleteDiscussion(discussionId);
        return new GenericResponse(null, true, null, null);
    }

    public GenericResponse sendMessage(Message message) throws SQLException {
        logger.log(Level.INFO, "Sending message: {0}", message.getContent());
        discussionDAO.sendMessage(message);
        return new GenericResponse(message, true, null, null);
    }

    public GenericResponse fetchDiscussionById(String discussionId) throws SQLException {
        logger.log(Level.INFO, "Fetching discussion by ID: {0}", discussionId);
        Discussion discussion = discussionDAO.getDiscussionById(discussionId);
        return new GenericResponse(discussion, true, null, null);
    }

    public GenericResponse updateMessage(Message message) throws SQLException {
        logger.log(Level.INFO, "Updating message: {0}", message.getId());
        Message updatedMessage = discussionDAO.updateMessage(message);
        if (updatedMessage != null) {
            return new GenericResponse(updatedMessage, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to update message", null);
    }

    public GenericResponse deleteMessage(String messageId) throws SQLException {
        logger.log(Level.INFO, "Deleting message: {0}", messageId);
        boolean isDeleted = discussionDAO.deleteMessage(messageId);
        if (isDeleted) {
            return new GenericResponse(null, true, null, null);
        }
        return new GenericResponse(null, false, "Failed to delete message", null);
    }

    public Discussion[] getRelevantDiscussions(String projectId, String userId) throws SQLException {
        logger.log(Level.INFO, "Getting relevant discussions for projectId: {0} and userId: {1}", new Object[]{projectId, userId});
        List<Discussion> allDiscussions = discussionDAO.getDiscussionsByProjectId(projectId);
        
        return allDiscussions.stream()
                .filter(discussion -> discussion.getParticipants().stream()
                        .anyMatch(participant -> participant.getUserId().equals(userId)))
                .toArray(Discussion[]::new);
    }

    public void createOrAddValidatorDiscussion(String projectId, String userId) {
        try {
            logger.log(Level.INFO, "Creating or updating validator discussion for project: {0}", projectId);
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);

            // Get all discussions for this project
            List<Discussion> discussions = discussionDAO.getDiscussionsByProjectId(projectId);
            
            // Try to find a discussion with title "Validator Team Discussion"
            Discussion validatorDiscussion = discussions.stream()
                .filter(d -> "Validator Team Discussion".equals(d.getTitle()))
                .findFirst()
                .orElse(null);
            
            if (validatorDiscussion != null) {
                // Discussion exists, check if validator needs to be added
                logger.log(Level.INFO, "Found existing validator discussion for project: {0}", projectId);
                
                // Get the user to add
                PublicUser validator = projectTeam.getProjectValidators().stream()
                    .filter(v -> v.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
                
                if (validator != null) {
                    // Check if validator is already a participant
                    boolean userExists = validatorDiscussion.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(userId));
                    
                    if (!userExists) {
                        // Add the new validator to the discussion
                        List<PublicUser> updatedParticipants = new ArrayList<>(validatorDiscussion.getParticipants());
                        updatedParticipants.add(new PublicUser(validator.getUserId(), validator.getName(), validator.getEmail()));
                        
                        // Create a new Discussion object with updated participants
                        Discussion updatedDiscussion = new Discussion(
                            validatorDiscussion.getId(),
                            validatorDiscussion.getTitle(),
                            updatedParticipants,
                            validatorDiscussion.getCreatedAt(),
                            validatorDiscussion.getProjectId(),
                            validatorDiscussion.getMessages()
                        );
                        
                        // Update the discussion participants
                        discussionDAO.updateDiscussion(updatedDiscussion);
                        logger.log(Level.INFO, "Added validator {0} to existing discussion", userId);
                    } else {
                        logger.log(Level.INFO, "Validator {0} is already in the discussion", userId);
                    }
                }
            } else {
                // Create new discussion
                List<PublicUser> participants = new ArrayList<>();
                
                // Add project lead
                participants.add(new PublicUser(projectTeam.getProjectLead().getUserId(), 
                                                projectTeam.getProjectLead().getName(),
                                                projectTeam.getProjectLead().getEmail()));
                
                // Add validators
                List<PublicUser> validators = projectTeam.getProjectValidators();
                if (validators != null && !validators.isEmpty()) {
                    for (PublicUser validator : validators) {
                        participants.add(new PublicUser(validator.getUserId(), 
                                                        validator.getName(), 
                                                        validator.getEmail()));
                    }
                    
                    Discussion discussion = new Discussion(
                        UUID.randomUUID().toString(), 
                        "Validator Team Discussion", 
                        participants, 
                        new Date(),
                        projectId, 
                        new ArrayList<>()
                    );
                    discussionDAO.createDiscussion(discussion);
                    logger.log(Level.INFO, "Successfully created validator discussion for project: {0}", projectId);
                } else {
                    logger.log(Level.WARNING, "No validators found for project: {0}", projectId);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating or updating validator discussion: {0}", e.getMessage());
        }
    }
    
    public void createValidatorHackerDiscussion(String projectId, String validatorId, String hackerId) {
        try {
            logger.log(Level.INFO, "Creating discussion between validator and hacker for project: {0}", projectId);
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
            
            // Find the validator
            PublicUser validator = projectTeam.getProjectValidators().stream()
                .filter(v -> v.getUserId().equals(validatorId))
                .findFirst()
                .orElse(null);

            PublicUser lead = projectTeam.getProjectLead();
                
            if (validator == null) {
                logger.log(Level.WARNING, "Validator {0} not found in project team", validatorId);
                return;
            }
            
            // Find the hacker
            PublicUser hacker = projectTeam.getProjectHackers().stream()
                .filter(h -> h.getUserId().equals(hackerId))
                .findFirst()
                .orElse(null);
                
            if (hacker == null) {
                logger.log(Level.WARNING, "Hacker {0} not found in project team", hackerId);
                return;
            }
            
            // Create participants list
            List<PublicUser> participants = new ArrayList<>();
            participants.add(validator);
            participants.add(hacker);
            participants.add(lead);
            
            // Create the discussion title
            String title = "Validator-Hacker Discussion: " + hacker.getName();
            
            Discussion discussion = new Discussion(
                UUID.randomUUID().toString(),
                title,
                participants,
                new Date(),
                projectId,
                new ArrayList<>()
            );
            
            discussionDAO.createDiscussion(discussion);
            logger.log(Level.INFO, "Successfully created validator-hacker discussion for project: {0}", projectId);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating validator-hacker discussion: {0}", e.getMessage());
        }
    }
    
    public void createLeadClientDiscussion(String projectId) {
        try {
            logger.log(Level.INFO, "Creating discussion between client and lead for project: {0}", projectId);
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);

            List<PublicUser> participants = new ArrayList<>();
            participants.add(new PublicUser(projectTeam.getClient().getUserId(), 
                                          projectTeam.getClient().getName(),
                                          projectTeam.getClient().getEmail()));
            participants.add(new PublicUser(projectTeam.getProjectLead().getUserId(), 
                                          projectTeam.getProjectLead().getName(),
                                          projectTeam.getProjectLead().getEmail()));

            Discussion discussion = new Discussion(
                UUID.randomUUID().toString(), 
                "Init Project", 
                participants, 
                new Date(),
                projectId, 
                new ArrayList<>()
            );
            
            discussionDAO.createDiscussion(discussion);
            logger.log(Level.INFO, "Successfully created lead-client discussion for project: {0}", projectId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating discussion with client and lead: {0}", e.getMessage());
        }
    }
}
