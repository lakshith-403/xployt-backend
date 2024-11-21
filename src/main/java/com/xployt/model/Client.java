package com.xployt.model;

public class Client {
  private int clientId;
  private String clientName;
  private String email;
  private String username;

  public Client() {
    // Default constructor
  }

  public Client(int clientId, String clientName, String email, String username) {
    this.clientId = clientId;
    this.clientName = clientName;
    this.email = email;
    this.username = username;
  }

  public int getClientId() {
    return clientId;
  }

  public void setClientId(int clientId) {
    this.clientId = clientId;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}