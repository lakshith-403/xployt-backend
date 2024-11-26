package com.xployt.dao.common;

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

import com.xployt.model.Attachment;
import com.xployt.model.Discussion;
import com.xployt.model.Message;
import com.xployt.model.PublicUser;
import com.xployt.util.ContextManager;
import com.xployt.util.CustomLogger;

public class DiscussionDAO {
    private static final Logger logger = CustomLogger.getLogger();

    public List<Discussion> getDiscussionsByProjectId(String projectId) throws SQLException {
        List<Discussion> discussions = new ArrayList<>();
        String sql = "SELECT d.*, u.* FROM Discussion d " +
                     "LEFT JOIN DiscussionParticipants dp ON d.id = dp.discussion_id " +
                     "LEFT JOIN Users u ON dp.user_id = u.userId " +
                     "WHERE d.project_id = ?";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, projectId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Discussion discussion = mapResultSetToDiscussion(rs, conn);
                discussions.add(discussion);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching discussions: {0}", e.getMessage());
            throw e;
        }
        return discussions;
    }

    public Discussion createDiscussion(Discussion discussion) throws SQLException {
        String sql = "INSERT INTO Discussion (id, title, project_id, created_at) VALUES (?, ?, ?, ?)";
        
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

    public void deleteDiscussion(String discussionId) throws SQLException {
        String sql = "DELETE FROM Discussion WHERE id = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.log(Level.INFO, "Discussion not found: {0}", discussionId);
            } else {
                deleteMessages(conn, discussionId);
                deleteParticipants(conn, discussionId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting discussion: {0}", e.getMessage());
            throw e;
        }
    }

    private void deleteMessages(Connection conn, String discussionId) throws SQLException {
        String sql = "DELETE FROM Message WHERE discussion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting messages for discussion: {0}", e.getMessage());
            throw e;
        }
    }

    public void updateDiscussion(Discussion discussion) throws SQLException {
        String sql = "UPDATE Discussion SET title = ?, project_id = ?, created_at = ? WHERE id = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussion.getTitle());
            stmt.setString(2, discussion.getProjectId());
            stmt.setTimestamp(3, new Timestamp(discussion.getCreatedAt().getTime()));
            stmt.setString(4, discussion.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.log(Level.INFO, "Discussion not found: {0}", discussion.getId());
            } else {
                updateParticipants(conn, discussion.getId(), discussion.getParticipants());
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating discussion: {0}", e.getMessage());
            throw e;
        }
    }

    private void updateParticipants(Connection conn, String discussionId, List<PublicUser> participants) throws SQLException {
        deleteParticipants(conn, discussionId);
        insertParticipants(conn, discussionId, participants);
    }

    private void deleteParticipants(Connection conn, String discussionId) throws SQLException {
        String sql = "DELETE FROM DiscussionParticipants WHERE discussion_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting participants for discussion: {0}", e.getMessage());
            throw e;
        }
    }

    public void insertParticipants(Connection conn, String discussionId, List<PublicUser> participants) throws SQLException {
        String sql = "INSERT INTO DiscussionParticipants (discussion_id, user_id) VALUES (?, ?)";

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

    public void removeParticipant(String discussionId, String userId) throws SQLException {
        String sql = "DELETE FROM DiscussionParticipants WHERE discussion_id = ? AND user_id = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            stmt.setString(2, userId);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                logger.log(Level.INFO, "Participant not found in discussion: {0}", discussionId);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error removing participant: {0}", e.getMessage());
            throw e;
        }
    }

    public void sendMessage(Message message) throws SQLException {
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        String sql = "INSERT INTO Message (id, discussion_id, sender_id, content, created_at, type) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getId());
            stmt.setString(2, message.getDiscussionId());
            stmt.setString(3, message.getSender().getUserId());
            stmt.setString(4, message.getContent());
            stmt.setTimestamp(5, new Timestamp(message.getTimestamp().getTime()));
            stmt.setString(6, message.getType());

            stmt.executeUpdate();

            insertAttachments(conn, message.getId(), message.getAttachments());
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error sending message: {0}", e.getMessage());
            throw e;
        }
    }

    private Discussion mapResultSetToDiscussion(ResultSet rs, Connection conn) throws SQLException {
        return new Discussion(
            rs.getString("id"),
            rs.getString("title"),
            getParticipantsForDiscussion(rs.getString("id"), conn),
            rs.getTimestamp("created_at"),
            rs.getString("project_id"),
            getMessagesForDiscussion(rs.getString("id"), conn)
        );
    }

    private List<PublicUser> getParticipantsForDiscussion(String discussionId, Connection conn) throws SQLException {
        List<PublicUser> participants = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                     "JOIN DiscussionParticipants dp ON u.userId = dp.user_id " +
                     "WHERE dp.discussion_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PublicUser user = new PublicUser(rs.getString("userId"), 
                rs.getString("name"), 
                rs.getString("email"));
                participants.add(user);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching participants: {0}", e.getMessage());
            throw e;
        }
        return participants;
    }

    private List<Message> getMessagesForDiscussion(String discussionId, Connection conn) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.* FROM Message m JOIN Users u ON m.sender_id = u.userId " +
                     "WHERE m.discussion_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message message = new Message(
                    rs.getString("id"),
                    new PublicUser(rs.getString("userId"), rs.getString("name"), rs.getString("email")),
                    rs.getString("content"),
                    getAttachmentsForMessage(rs.getString("id"), conn),
                    new java.util.Date(rs.getTimestamp("created_at").getTime()),
                    rs.getString("type"), 
                    discussionId
                );
                messages.add(message);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching messages: {0}", e.getMessage());
            throw e;
        }
        return messages;
    }

    private List<Attachment> getAttachmentsForMessage(String messageId, Connection conn) throws SQLException {
        List<Attachment> attachments = new ArrayList<>();
        String sql = "SELECT a.* FROM Attachment a JOIN MessageAttachments ma ON a.id = ma.attachment_id " +
                     "WHERE ma.message_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, messageId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attachment attachment = new Attachment(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("url")
                );
                attachments.add(attachment);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching attachments: {0}", e.getMessage());
            throw e;
        }
        return attachments;
    }

    private void insertAttachments(Connection conn, String messageId, List<Attachment> attachments) throws SQLException {
        String sqlInsertMessageAttachments = "INSERT INTO MessageAttachments (message_id, attachment_id) VALUES (?, ?)";
        String sqlInsertAttachment = "INSERT INTO Attachment (id, name, url) VALUES (?, ?, ?)";
        try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertMessageAttachments);
             PreparedStatement stmtInsertAttachment = conn.prepareStatement(sqlInsertAttachment)) {
            for (Attachment attachment : attachments) {
                stmtInsertAttachment.setString(1, attachment.getId());
                stmtInsertAttachment.setString(2, attachment.getName());
                stmtInsertAttachment.setString(3, attachment.getUrl());
                stmtInsertAttachment.executeUpdate();

                stmtInsert.setString(1, messageId);
                stmtInsert.setString(2, attachment.getId());
                stmtInsert.executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting or updating attachments: {0}", e.getMessage());
            throw e;
        }
    }

    public Discussion getDiscussionById(String discussionId) throws SQLException {
        String sql = "SELECT * FROM Discussion WHERE id = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, discussionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDiscussion(rs, conn);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching discussion by ID: {0}", e.getMessage());
            throw e;
        }
        return null;
    }

    public Message updateMessage(Message message) throws SQLException {
        String sql = "UPDATE Message SET content = ? WHERE id = ?";
        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, message.getContent());
            stmt.setString(2, message.getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating message failed, no rows affected.");
            }
            return message;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating message: {0}", e.getMessage());
            throw e;
        }
    }

    public boolean deleteMessage(String messageId) throws SQLException {
        String sqlDeleteMessage = "DELETE FROM Message WHERE id = ?";
        String sqlDeleteMessageAttachments = "DELETE FROM MessageAttachments WHERE message_id = ?";
        String sqlDeleteAttachments = "DELETE FROM Attachment WHERE id IN (SELECT attachment_id FROM MessageAttachments WHERE message_id = ?)";

        ServletContext servletContext = ContextManager.getContext("DBConnection");
        Connection conn = (Connection) servletContext.getAttribute("DBConnection");

        try {
            // Start a transaction
            conn.setAutoCommit(false);

            // Delete from MessageAttachments first
            try (PreparedStatement stmtDeleteMessageAttachments = conn.prepareStatement(sqlDeleteMessageAttachments)) {
                stmtDeleteMessageAttachments.setString(1, messageId);
                stmtDeleteMessageAttachments.executeUpdate();
            }

            // Delete from Attachment
            try (PreparedStatement stmtDeleteAttachments = conn.prepareStatement(sqlDeleteAttachments)) {
                stmtDeleteAttachments.setString(1, messageId);
                stmtDeleteAttachments.executeUpdate();
            }

            // Finally, delete the message
            try (PreparedStatement stmtDeleteMessage = conn.prepareStatement(sqlDeleteMessage)) {
                stmtDeleteMessage.setString(1, messageId);
                stmtDeleteMessage.executeUpdate();
            }

            // Commit the transaction
            conn.commit();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting message and its attachments: {0}", e.getMessage());
            // Rollback in case of an error
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true); // Reset auto-commit to true
        }
        return true;
    }
}
