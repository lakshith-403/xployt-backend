package com.xployt.dao.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.model.Complaint;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class ComplaintDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public void createComplaint(Complaint complaint) throws SQLException {
        String sql = "INSERT INTO complaints (title, notes, project_id, created_by, created_at, team_members, discussion_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, complaint.getTitle());
            pstmt.setString(2, complaint.getNotes());
            pstmt.setString(3, complaint.getProjectId());
            pstmt.setString(4, complaint.getCreatedBy());
            pstmt.setTimestamp(5, new Timestamp(complaint.getCreatedAt().getTime()));
            pstmt.setString(6, String.join(",", complaint.getTeamMembers()));
            pstmt.setString(7, complaint.getDiscussionId());

            pstmt.executeUpdate();
            logger.log(Level.INFO, "Complaint created successfully with ID: {0}", complaint.getId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating complaint: {0}", e.getMessage());
            throw e;
        }
    }

    public Complaint getComplaintById(String complaintId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE id = ?";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, complaintId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToComplaint(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving complaint: {0}", e.getMessage());
            throw e;
        }
        
        return null;
    }

    public List<Complaint> getComplaintsByProjectId(String projectId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE project_id = ? ORDER BY created_at DESC";
        List<Complaint> complaints = new ArrayList<>();
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, projectId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                complaints.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving complaints for project: {0}", e.getMessage());
            throw e;
        }
        
        return complaints;
    }

    public List<Complaint> getComplaintsByUser(String userId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE created_by = ? OR team_members LIKE ? ORDER BY created_at DESC";
        List<Complaint> complaints = new ArrayList<>();
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, "%" + userId + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                complaints.add(mapResultSetToComplaint(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving complaints for user: {0}", e.getMessage());
            throw e;
        }
        
        return complaints;
    }

    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        try {
            int id = rs.getInt("id");
            String title = rs.getString("title");
            String notes = rs.getString("notes");
            String projectId = rs.getString("project_id");
            String createdBy = rs.getString("created_by");
            Date createdAt = rs.getTimestamp("created_at");
            String teamMembersStr = rs.getString("team_members");
            List<String> teamMembers = new ArrayList<>();
            if (teamMembersStr != null && !teamMembersStr.isEmpty()) {
                teamMembers = Arrays.asList(teamMembersStr.split(","));
            }
            String discussionId = rs.getString("discussion_id");
            boolean resolved = rs.getBoolean("resolved");

            Complaint complaint = new Complaint(id, title, notes, projectId, createdBy, createdAt, teamMembers, discussionId);
            complaint.setResolved(resolved);
            return complaint;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error mapping result set to complaint: {0}", e.getMessage());
            throw e;
        }
    }

    public Complaint getComplaintByDiscussionId(String discussionId) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE discussion_id = ?";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, discussionId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToComplaint(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving complaint by discussion ID: {0}", e.getMessage());
            throw e;
        }
        
        return null;
    }

    public void updateComplaint(Complaint complaint) throws SQLException {
        String sql = "UPDATE complaints SET title = ?, notes = ?, project_id = ?, created_by = ?, " +
                    "team_members = ?, discussion_id = ?, resolved = ? WHERE id = ?";
                
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, complaint.getTitle());
            pstmt.setString(2, complaint.getNotes());
            pstmt.setString(3, complaint.getProjectId());
            pstmt.setString(4, complaint.getCreatedBy());
            pstmt.setString(5, String.join(",", complaint.getTeamMembers()));
            pstmt.setString(6, complaint.getDiscussionId());
            pstmt.setBoolean(7, complaint.isResolved());
            pstmt.setInt(8, complaint.getId());
            
            pstmt.executeUpdate();
            logger.log(Level.INFO, "Complaint updated successfully with ID: {0}", complaint.getId());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating complaint: {0}", e.getMessage());
            throw e;
        }
    }
} 