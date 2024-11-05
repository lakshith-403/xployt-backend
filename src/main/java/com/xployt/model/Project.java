package com.xployt.model;

public class Project {
    private int id;
    private String status;
    private String title;
    private String client;
    private int pendingReports;

    public Project(int id, String status, String title, String client, int pendingReports) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.client = client;
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

    public String getClient() {
        return client;
    }

    public int getPendingReports() {
        return pendingReports;
    }

}
