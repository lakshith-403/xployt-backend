package com.xployt.model;

public class CreateProject {

  private String clientId;
  private String title;
  private String description;
  private String startDate;
  private String endDate;
  private String url;
  private String technicalStack;

  // Getters and setters
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

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
}
