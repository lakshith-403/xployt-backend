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

    //    Getters and Setters
    public int getProjectId() {
        return projectId;
    }

    public int getHackerId() {
        return hackerId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
