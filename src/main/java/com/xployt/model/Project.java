package com.xployt.model;

public class Project {
    private int id;
    private String status;
    private String title;
    private String client;
    private int pendingReports;
    private String startDate;
    private String endDate;
    private String url;
    private String technicalStack;

    public Project(int id, String status, String title, String client, int pendingReports, String startDate,
            String endDate, String url, String technicalStack) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.client = client;
        this.pendingReports = pendingReports;
        this.startDate = startDate;
        this.endDate = endDate;
        this.url = url;
        this.technicalStack = technicalStack;
    }

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

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getUrl() {
        return url;
    }

    public String getTechnicalStack() {
        return technicalStack;
    }
}
