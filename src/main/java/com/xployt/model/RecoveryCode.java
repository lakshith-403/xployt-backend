package com.xployt.model;

import java.time.LocalDateTime;

public class RecoveryCode {
    private final String id;
    private final String email;
    private final String code;
    private final LocalDateTime createdAt;

    public RecoveryCode(String id, String email, String code, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isValid() {
        // Check if the code is less than 5 minutes old
        return createdAt.plusMinutes(5).isAfter(LocalDateTime.now());
    }
} 