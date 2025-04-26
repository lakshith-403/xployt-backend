package com.xployt.model;

public class PublicUser {
    private final String userId;
    private final String name;
    private final String email;
    private final String role;

    public PublicUser(String userId, String name, String email ) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = "";
    }

    public PublicUser(String userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
