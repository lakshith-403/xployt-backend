package com.xployt.model;

public class ProjectBriefLead {
    private int id;
    private String status;
    private String title;
    private String clientId;
    private int pendingReports;

    public ProjectBriefLead(int id, String status, String title, String clientId, int pendingReports) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.clientId = clientId;
        this.pendingReports = pendingReports;
    }

    // Getters and setters omitted for brevity
    public int getId() {
        return id;
    }

    public String getStatus() {
        return status;
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
