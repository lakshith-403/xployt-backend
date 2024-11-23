package com.xployt.service;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.xployt.dao.DiscussionDAO;
import com.xployt.model.Discussion;
import com.xployt.model.GenericResponse;
import com.xployt.model.Message;
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
} 


