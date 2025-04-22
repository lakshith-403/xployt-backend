package com.xployt.service.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.dao.common.DiscussionDAO;
import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.model.Attachment;
import com.xployt.model.Complaint;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class DiscussionService {
    private final DiscussionDAO discussionDAO;
    private static final Logger logger = CustomLogger.getLogger();

    public DiscussionService() {
        this.discussionDAO = new DiscussionDAO();
    }

    public Discussion[] fetchDiscussions(String projectId) throws SQLException {
        logger.log(Level.INFO, "Fetching discussions for projectId: {0}", projectId);
        List<Discussion> discussions = discussionDAO.getDiscussionsByProjectId(projectId);
        return discussions.toArray(new Discussion[0]);
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

    public void notifyReportySubmission(String projectId, String userId, String reportId) {
        try {
            logger.log(Level.INFO, "Notifying report submission for project: {0}, user: {1}, report: {2}", new Object[]{projectId, userId, reportId});
            
            // Get all discussions for the project
            List<Discussion> discussions = discussionDAO.getDiscussionsByProjectId(projectId);
            
            // Get user details
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
            
            // Find the hacker by userId
            PublicUser hacker = projectTeam.getProjectHackers().stream()
                .filter(h -> h.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
            
            if (hacker == null) {
                logger.log(Level.WARNING, "Hacker not found for userId: {0}", userId);
                return;
            }
            
            // For each discussion where the user is a participant
            for (Discussion discussion : discussions) {
                boolean isParticipant = discussion.getParticipants().stream()
                    .anyMatch(p -> p.getUserId().equals(userId));
                
                if (isParticipant) {
                    // Create message content
                    String content = "Hacker " + hacker.getName() + " submitted a report";
                    
                    // Create attachment for the report
                    List<Attachment> attachments = new ArrayList<>();
                    attachments.add(new Attachment(
                        UUID.randomUUID().toString(),
                        "Report-" + reportId,
                        "/reports/vulnerability/" + projectId + "/" + reportId
                    ));
                    
                    // Create and send the message
                    Message message = new Message(
                        UUID.randomUUID().toString(),
                        hacker,
                        content,
                        attachments,
                        new Date(),
                        "NOTIFICATION",
                        discussion.getId()
                    );
                    
                    discussionDAO.sendMessage(message);
                    logger.log(Level.INFO, "Notification message sent to discussion: {0}", discussion.getId());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error notifying report submission: {0}", e.getMessage());
        }
    }
    
    public void notifyComplaintSubmissions(String projectId, String complaintId) {
        try {
            logger.log(Level.INFO, "Notifying complaint submission for project: {0}, complaint: {1}", new Object[]{projectId, complaintId});
            
            // Get the complaint
            Complaint complaint = getComplaintById(complaintId);
            if (complaint == null) {
                logger.log(Level.WARNING, "Complaint not found for id: {0}", complaintId);
                return;
            }
            
            // Get user who created the complaint
            ProjectTeamDAO projectTeamDAO = new ProjectTeamDAO();
            ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
            
            // Find the user who created the complaint
            PublicUser complainant = null;
            
            // Check if user is lead
            if (projectTeam.getProjectLead().getUserId().equals(complaint.getCreatedBy())) {
                complainant = projectTeam.getProjectLead();
            } 
            // Check if user is client
            else if (projectTeam.getClient().getUserId().equals(complaint.getCreatedBy())) {
                complainant = projectTeam.getClient();
            } 
            // Check if user is hacker
            else {
                complainant = projectTeam.getProjectHackers().stream()
                    .filter(h -> h.getUserId().equals(complaint.getCreatedBy()))
                    .findFirst()
                    .orElse(null);
                
                // Check if user is validator
                if (complainant == null) {
                    complainant = projectTeam.getProjectValidators().stream()
                        .filter(v -> v.getUserId().equals(complaint.getCreatedBy()))
                        .findFirst()
                        .orElse(null);
                }
            }
            
            if (complainant == null) {
                logger.log(Level.WARNING, "User not found for userId: {0}", complaint.getCreatedBy());
                return;
            }
            
            // Get all discussions for the project
            List<Discussion> discussions = discussionDAO.getDiscussionsByProjectId(projectId);
            
            // Create message content
            String content = "User " + complainant.getName() + " submitted a complaint";
            
            // For each discussion where any of the relevant parties are participants
            for (Discussion discussion : discussions) {
                // Skip complaint discussions - they start with "Complaint: "
                if (discussion.getTitle().startsWith("Complaint: ")) {
                    continue;
                }
                
                // Check if any team member of the complaint is a participant in this discussion
                boolean hasRelevantParticipant = false;
                
                for (String teamMemberId : complaint.getTeamMembers()) {
                    hasRelevantParticipant = discussion.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(teamMemberId));
                    
                    if (hasRelevantParticipant) {
                        break;
                    }
                }
                
                // Also check if the complainant is a participant
                if (!hasRelevantParticipant) {
                    hasRelevantParticipant = discussion.getParticipants().stream()
                        .anyMatch(p -> p.getUserId().equals(complaint.getCreatedBy()));
                }
                
                if (hasRelevantParticipant) {
                    // Create attachment for the complaint
                    List<Attachment> attachments = new ArrayList<>();
                    attachments.add(new Attachment(
                        UUID.randomUUID().toString(),
                        "Complaint-" + complaintId,
                        "/complaints/" + complaintId
                    ));
                    
                    // Create and send the message
                    Message message = new Message(
                        UUID.randomUUID().toString(),
                        complainant,
                        content,
                        attachments,
                        new Date(),
                        "NOTIFICATION",
                        discussion.getId()
                    );
                    
                    discussionDAO.sendMessage(message);
                    logger.log(Level.INFO, "Complaint notification message sent to discussion: {0}", discussion.getId());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error notifying complaint submission: {0}", e.getMessage());
        }
    }
    
    private Complaint getComplaintById(String complaintId) {
        try {
            // This is a simplified method that would be replaced with actual complaint retrieval logic
            // In a real implementation, you would have a ComplaintDAO or similar to fetch the complaint
            int complaintIdInt = Integer.parseInt(complaintId);
            
            ServletContext servletContext = ContextManager.getContext("DBConnection");
            Connection conn = (Connection) servletContext.getAttribute("DBConnection");
            
            String sql = "SELECT * FROM Complaint WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, complaintIdInt);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Complaint complaint = new Complaint();
                    complaint.setId(rs.getInt("id"));
                    complaint.setTitle(rs.getString("title"));
                    complaint.setNotes(rs.getString("notes"));
                    complaint.setProjectId(rs.getString("project_id"));
                    complaint.setCreatedBy(rs.getString("created_by"));
                    complaint.setCreatedAt(rs.getTimestamp("created_at"));
                    complaint.setDiscussionId(rs.getString("discussion_id"));
                    complaint.setResolved(rs.getBoolean("resolved"));
                    
                    // Get team members
                    List<String> teamMembers = new ArrayList<>();
                    String membersSql = "SELECT user_id FROM ComplaintTeamMembers WHERE complaint_id = ?";
                    try (PreparedStatement membersStmt = conn.prepareStatement(membersSql)) {
                        membersStmt.setInt(1, complaintIdInt);
                        ResultSet membersRs = membersStmt.executeQuery();
                        
                        while (membersRs.next()) {
                            teamMembers.add(membersRs.getString("user_id"));
                        }
                    }
                    
                    complaint.setTeamMembers(teamMembers);
                    return complaint;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving complaint: {0}", e.getMessage());
        }
        return null;
    }
}
