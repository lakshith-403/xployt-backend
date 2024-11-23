package com.xployt.model;

import java.util.Date;
import java.util.List;

public class Discussion {
    private final String id;
    private final String title;
    private final List<PublicUser> participants;
    private final Date createdAt;
    private final String projectId;
    private final List<Message> messages;

    public Discussion(String id, String title, List<PublicUser> participants, Date createdAt, String projectId, List<Message> messages) {
        this.id = id;
        this.title = title;
        this.participants = participants;
        this.createdAt = createdAt;
        this.projectId = projectId;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<PublicUser> getParticipants() {
        return participants;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<Message> getMessages() {
        return messages;
    }
} 