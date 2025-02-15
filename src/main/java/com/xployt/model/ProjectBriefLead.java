package com.xployt.model;

public class ProjectBriefLead {
    private int id;
    private String state;
    private String title;
    private String clientId;
    private int pendingReports;

    public ProjectBriefLead(int id, String state, String title, String clientId, int pendingReports) {
        this.id = id;
        this.state = state;
        this.title = title;
        this.clientId = clientId;
        this.pendingReports = pendingReports;
    }

    // Getters and setters omitted for brevity
    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public String getClientId() {
        return clientId;
    }

    public int getPendingReports() {
        return pendingReports;
    }

}
