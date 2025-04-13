package com.xployt.controller.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xployt.dao.common.ComplaintDAO;
import com.xployt.dao.common.DiscussionDAO;
import com.xployt.dao.common.ProjectTeamDAO;
import com.xployt.dao.common.UserDAO;
import com.xployt.model.Complaint;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.ProjectTeam;
import com.xployt.model.PublicUser;
import com.xployt.model.User;
import com.xployt.util.AuthUtil;
import com.xployt.util.CustomLogger;
import com.xployt.util.JsonUtil;

@WebServlet("/api/complaints/*")
public class ComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = CustomLogger.getLogger();
    private ComplaintDAO complaintDAO;
    private DiscussionDAO discussionDAO;
    private ProjectTeamDAO projectTeamDAO;
    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        complaintDAO = new ComplaintDAO();
        discussionDAO = new DiscussionDAO();
        projectTeamDAO = new ProjectTeamDAO();
        userDAO = new UserDAO();
        gson = JsonUtil.useGson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = AuthUtil.getSignedInUser(request);
        
        if (user == null) {
            sendUnauthorized(response);
            return;
        }

        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all complaints for user
                List<Complaint> complaints = complaintDAO.getComplaintsByUser(user.getUserId());
                sendJsonResponse(response, complaints);
            } else if (pathInfo.startsWith("/discussion/")) {
                // Get complaint by discussion ID
                String discussionId = pathInfo.substring("/discussion/".length());
                Complaint complaint = complaintDAO.getComplaintByDiscussionId(discussionId);
                
                if (complaint == null) {
                    sendNotFound(response, "Complaint not found for the given discussion");
                    return;
                }
                
                sendJsonResponse(response, complaint);
            } else {
                // Get specific complaint
                String complaintId = pathInfo.substring(1);
                Complaint complaint = complaintDAO.getComplaintById(complaintId);
                
                if (complaint == null) {
                    sendNotFound(response, "Complaint not found");
                    return;
                }
                
                // Check if user has access to this complaint
                ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(complaint.getProjectId());
                boolean hasAccess = false;
                
                if (complaint.getCreatedBy().equals(user.getUserId())) {
                    hasAccess = true;
                } else if (projectTeam.getProjectLead() != null && user.getUserId().equals(projectTeam.getProjectLead().getUserId())) {
                    hasAccess = true;
                } else if (complaint.getTeamMembers().contains(user.getUserId())) {
                    hasAccess = true;
                }
                
                if (!hasAccess) {
                    sendUnauthorized(response);
                    return;
                }
                
                sendJsonResponse(response, complaint);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving complaints: {0}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving complaints");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = AuthUtil.getSignedInUser(request);
        
        if (user == null) {
            sendUnauthorized(response);
            return;
        }

        try {
            // Parse JSON request using Gson
            JsonObject requestJson = gson.fromJson(new InputStreamReader(request.getInputStream()), JsonObject.class);
            
            // Validate required fields
            if (!requestJson.has("title") || !requestJson.has("notes") || !requestJson.has("projectId")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing required fields");
                return;
            }
            
            String title = requestJson.get("title").getAsString();
            String notes = requestJson.get("notes").getAsString();
            String projectId = requestJson.get("projectId").getAsString();
            
            // Get team members from request
            List<String> teamMembers = new ArrayList<>();
            if (requestJson.has("teamMembers") && !requestJson.get("teamMembers").isJsonNull()) {
                JsonArray teamMembersArray = requestJson.getAsJsonArray("teamMembers");
                for (JsonElement element : teamMembersArray) {
                    teamMembers.add(element.getAsString());
                }
            }
            
            // Create a discussion for the complaint
            String discussionId = createComplaintDiscussion(title, projectId, user, teamMembers);
            
            Complaint complaint = new Complaint(
             0,    
                title,
                notes,
                projectId,
                user.getUserId(),
                new Date(),
                teamMembers,
                discussionId
            );
            
            complaintDAO.createComplaint(complaint);
            
            // Send success response with appropriate GenericResponse constructor
            GenericResponse genericResponse = new GenericResponse(
            "",    
            true,         // is_successful is true
                "Complaint created successfully",  // message
                null          // no error
            );
            
            sendJsonResponse(response, genericResponse);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating complaint: {0}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating complaint: " + e.getMessage());
        }
    }

    private String createComplaintDiscussion(String title, String projectId, User creator, List<String> teamMembers) throws SQLException {
        List<PublicUser> participants = new ArrayList<>();
        
        // Add the complaint creator
        participants.add(new PublicUser(creator.getUserId(), creator.getName(), creator.getEmail()));
        
        // Add project lead
        ProjectTeam projectTeam = projectTeamDAO.getProjectTeam(projectId);
        if (projectTeam.getProjectLead() != null) {
            PublicUser lead = projectTeam.getProjectLead();
            if (!lead.getUserId().equals(creator.getUserId())) {
                participants.add(lead);
            }
        }
        
        // Add selected team members
        for (String userId : teamMembers) {
            User teamMember = userDAO.getUserById(userId);
            if (teamMember != null && !userId.equals(creator.getUserId())) {
                participants.add(new PublicUser(teamMember.getUserId(), teamMember.getName(), teamMember.getEmail()));
            }
        }
        
        // Create and save the discussion
        String discussionId = UUID.randomUUID().toString();
        Discussion discussion = new Discussion(
            discussionId,
            "Complaint: " + title,
            participants,
            new Date(),
            projectId,
            new ArrayList<>()
        );
        
        discussionDAO.createDiscussion(discussion);
        return discussionId;
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);
        
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("status", "error");
        errorJson.addProperty("message", errorMessage);
        
        response.getWriter().write(gson.toJson(errorJson));
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access");
    }

    private void sendNotFound(HttpServletResponse response, String message) throws IOException {
        sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, message);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = AuthUtil.getSignedInUser(request);
        
        if (user == null) {
            sendUnauthorized(response);
            return;
        }
        
        if (!"ProjectLead".equals(user.getRole())) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Only project leads can resolve complaints");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Discussion ID is required");
            return;
        }
        
        // The URL format is /api/complaints/{discussionId}
        String discussionId = pathInfo.substring(1);
        
        try {
            // Find the complaint by discussion ID
            Complaint complaint = complaintDAO.getComplaintByDiscussionId(discussionId);
            
            if (complaint == null) {
                sendNotFound(response, "Complaint not found for the given discussion");
                return;
            }
            
            // Mark the complaint as resolved
            complaint.setResolved(true);
            complaintDAO.updateComplaint(complaint);
            
            // Return success response
            GenericResponse genericResponse = new GenericResponse(
                "",
                true,
                "Complaint resolved successfully",
                null
            );
            
            sendJsonResponse(response, genericResponse);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error resolving complaint: {0}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error resolving complaint: " + e.getMessage());
        }
    }
} 