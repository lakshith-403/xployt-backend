package com.xployt.model;

public class Invitation {
    private int hackerId;
    private int projectId;
    private String status;
    private String timestamp;

    public Invitation(int hackerId, int projectId, String status, String timestamp) {
        this.projectId = projectId;
        this.hackerId = hackerId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Invitation() {}

    //    Getters and Setters
    public int getProjectId() {
        return projectId;
    }

    public int getHackerId() {
        return hackerId;
    }

    public String getStatus() { return status; }

    public String getTimestamp() { return timestamp; }

    public void setHackerId(String hackerId) {
        this.hackerId = Integer.parseInt(hackerId);
    }

    public void setProjectId(String projectId) {
        this.projectId = Integer.parseInt(projectId);
    }

    public void setStatus(String status) { this.status = status; }
}
