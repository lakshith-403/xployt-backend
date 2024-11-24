package com.xployt.model;

import java.util.Date;
import java.util.List;

public class Message {
    private final String id;
    private final PublicUser sender;
    private final String content;
    private final List<Attachment> attachments;
    private final Date timestamp;
    private final String type;
    private final String discussionId;

    public Message(String id, PublicUser sender, String content, List<Attachment> attachments, Date timestamp, String type, String discussionId) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.attachments = attachments;
        this.timestamp = timestamp;
        this.type = type;
        this.discussionId = discussionId;
    }

    public String getId() {
        return id;
    }

    public PublicUser getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getDiscussionId() {
        return discussionId;
    }
} 