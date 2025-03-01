package com.xployt.model;

public class Hacker extends PublicUser {
    private int points;

    public Hacker(String userId, String name, String email, int points) {
        super( userId, name, email);
        this.points = points;
    }

    // Getters and Setters
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) { this.points = points; }
}
