package com.xployt.model;

public class Project {

  private String projectId;
  private String clientId;
  private String leadId;
  private String title;
  private String description;
  private String startDate;
  private String endDate;
  private String url;
  private String technicalStack;
  private String[] scope;

  // Getters and setters
  public String getProjectId() { return projectId; }

  public void setProjectId(String projectId) { this.projectId = projectId; }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getLeadId() { return leadId; }

  public void setLeadId(String leadId) { this.leadId = leadId; }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStartDate() {
    return startDate;
  }

  public void setStartDate(String startDate) {
    this.startDate = startDate;
  }

  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTechnicalStack() {
    return technicalStack;
  }

  public void setTechnicalStack(String technicalStack) {
    this.technicalStack = technicalStack;
  }

  public String[] getScope() { return scope; }

  public void setScope(String[] scope) { this.scope = scope; }
}
