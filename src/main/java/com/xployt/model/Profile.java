package com.xployt.model;

public class Profile {
    private int userId;
    private String name;
    private String email;
    private String phone;

    // private double fundsRemaining;
    // private double fundsSpent;

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    // public String getProfilePicture() { return profilePicture; }
    // public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    // public double getFundsRemaining() { return fundsRemaining; }
    // public void setFundsRemaining(double fundsRemaining) { this.fundsRemaining = fundsRemaining; }

    // public double getFundsSpent() { return fundsSpent; }
    // public void setFundsSpent(double fundsSpent) { this.fundsSpent = fundsSpent; }
}
