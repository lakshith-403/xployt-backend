package com.xployt.model;

public class User {
    private final String userId;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final String role;
    private final String createdAt;
    private final String updatedAt;
    private final String status;

    public User(String userId, String email, String passwordHash, String name, String role, String createdAt, String updatedAt, String status) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getStatus() {
        return status;
    }
} 