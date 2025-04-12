package com.xployt.model;

import java.util.Date;
import java.util.List;

public class Complaint {
    private int id;
    private String title;
    private String notes;
    private String projectId;
    private String createdBy;
    private Date createdAt;
    private List<String> teamMembers;
    private String discussionId;
    private boolean resolved;

    public Complaint() {
    }

    public Complaint(int id, String title, String notes, String projectId, String createdBy, 
                    Date createdAt, List<String> teamMembers, String discussionId) {
        this.id = id;
        this.title = title;
        this.notes = notes;
        this.projectId = projectId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.teamMembers = teamMembers;
        this.discussionId = discussionId;
        this.resolved = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<String> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    public void setDiscussionId(String discussionId) {
        this.discussionId = discussionId;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
} 