package com.xployt.model;

public class ProjectHacker {
    private int projectHackerId;
    private int projectId;
    private int hackerId;
    private int assignedValidatorId;
    private String status; //0 - Invited, 1 - Accepted
    private String timestamp;

    public ProjectHacker(int projectHackerId, int projectId, int hackerId, int assignedValidatorId, String status, String timestamp) {
        this.projectHackerId = projectHackerId;
        this.projectId = projectId;
        this.hackerId = hackerId;
        this.assignedValidatorId = assignedValidatorId;
        this.status = status;
        this.timestamp = timestamp;
    }

    //    Getters and Setters
    public int getProjectHackerId() {
        return projectHackerId;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getHackerId() {
        return hackerId;
    }

    public int getAssignedValidatorId() {
        return assignedValidatorId;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
