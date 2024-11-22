package com.xployt.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import com.xployt.model.Discussion;
import com.xployt.model.Message;
import com.xployt.model.PublicUser;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class DiscussionDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public List<Discussion> getDiscussionsByProjectId(String projectId) throws SQLException {
        List<Discussion> discussions = new ArrayList<>();
        String sql = "SELECT d.*, u.* FROM discussions d " +
                     "LEFT JOIN discussion_participants dp ON d.id = dp.discussion_id " +
                     "LEFT JOIN users u ON dp.user_id = u.id " +
                     "WHERE d.project_id = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Discussion discussion = mapResultSetToDiscussion(rs);
                discussions.add(discussion);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching discussions: {0}", e.getMessage());
            throw e;
        }
        return discussions;
    }

    public Discussion createDiscussion(Discussion discussion) throws SQLException {
        String sql = "INSERT INTO discussions (id, title, project_id, created_at) VALUES (?, ?, ?, ?)";
        
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, discussion.getId());
            stmt.setString(2, discussion.getTitle());
            stmt.setString(3, discussion.getProjectId());
            stmt.setTimestamp(4, new Timestamp(discussion.getCreatedAt().getTime()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                insertParticipants(conn, discussion.getId(), discussion.getParticipants());
                return discussion;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating discussion: {0}", e.getMessage());
            throw e;
        }
        return null;
    }

    private void insertParticipants(Connection conn, String discussionId, List<PublicUser> participants) throws SQLException {
        String sql = "INSERT INTO discussion_participants (discussion_id, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (PublicUser participant : participants) {
                stmt.setString(1, discussionId);
                stmt.setString(2, participant.getUserId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting participants: {0}", e.getMessage());
            throw e;
        }
    }

    private Discussion mapResultSetToDiscussion(ResultSet rs) throws SQLException {
        return new Discussion(
            rs.getString("id"),
            rs.getString("title"),
            getParticipantsForDiscussion(rs.getString("id")),
            rs.getTimestamp("created_at"),
            rs.getString("project_id"),
            getMessagesForDiscussion(rs.getString("id"))
        );
    }

    private List<PublicUser> getParticipantsForDiscussion(String discussionId) throws SQLException {
        List<PublicUser> participants = new ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                     "JOIN discussion_participants dp ON u.id = dp.user_id " +
                     "WHERE dp.discussion_id = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PublicUser user = new PublicUser(rs.getString("id"), 
                rs.getString("username"), 
                rs.getString("email"));
                participants.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching participants: {0}", e.getMessage());
            throw e;
        }
        return participants;
    }

    private List<Message> getMessagesForDiscussion(String discussionId) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.* FROM messages m JOIN users u ON m.user_id = u.id " +
                     "WHERE m.discussion_id = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                    rs.getString("id"),
                    new PublicUser(rs.getString("user_id"), rs.getString("username"), rs.getString("email")),
                    rs.getString("content"),
                    new ArrayList<>(), // todo fetch attachments
                    new java.util.Date(rs.getTimestamp("created_at").getTime()),
                    rs.getString("type")
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching messages: {0}", e.getMessage());
            throw e;
        }
        return messages;
    }
} 