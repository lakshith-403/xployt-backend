package com.xployt.model;

public class ProjectBrief {
    private int id;
    private String state;
    private String title;
    private String leadId;
    private String clientId;
    private String startDate;
    private String endDate;
    private int pendingReports;

    public ProjectBrief(int id, String state, String title, String leadId, String clientId, String startDate, String endDate, int pendingReports) {
        this.id = id;
        this.state = state;
        this.title = title;
        this.leadId = leadId;
        this.clientId = clientId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pendingReports = pendingReports;
    }

    // Getters and setters omitted for brevity
    public int getId() {
        return id;
    }

    public String getStatus() {
        return state;
    }

    public String getTitle() {
        return title;
    }

    public String getLeadId() {
        return leadId;
    }

    public String getClientId() { return clientId; }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getPendingReports() {
        return pendingReports;
    }
}