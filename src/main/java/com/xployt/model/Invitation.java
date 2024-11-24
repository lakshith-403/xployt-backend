package com.xployt.model;

public class Invitation {
    private int hackerId;
    private int projectId;
    private String timestamp;

    public Invitation(int hackerId, int projectId, String timestamp) {
        this.projectId = projectId;
        this.hackerId = hackerId;
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

    public String getTimestamp() { return timestamp; }

    public void setHackerId(String hackerId) {
        this.hackerId = Integer.parseInt(hackerId);
    }

    public void setProjectId(String projectId) {
        this.projectId = Integer.parseInt(projectId);
    }
}
