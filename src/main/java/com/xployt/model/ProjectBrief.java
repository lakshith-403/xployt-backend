package com.xployt.model;

public class ProjectBrief {
    private int id;
    private String state;
    private String title;
    private String leadId;
    private int pendingReports;

    public ProjectBrief(int id, String state, String title, String leadId, int pendingReports) {
        this.id = id;
        this.state = state;
        this.title = title;
        this.leadId = leadId;
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

    public String getLeadId() {
        return leadId;
    }

    public int getPendingReports() {
        return pendingReports;
    }

}
