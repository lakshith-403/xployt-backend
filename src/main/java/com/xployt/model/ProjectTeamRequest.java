package com.xployt.model;

public class ProjectTeamRequest {
    private String projectId;
    private String clientId;
    private String projectLeadId;
    private int numberOfValidators;

    // Getters and setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getProjectLeadId() { return projectLeadId; }
    public void setProjectLeadId(String projectLeadId) { this.projectLeadId = projectLeadId; }
    
    public int getNumberOfValidators() { return numberOfValidators; }
    public void setNumberOfValidators(int numberOfValidators) { this.numberOfValidators = numberOfValidators; }
} 