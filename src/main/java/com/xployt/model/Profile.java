package com.xployt.model;

public class Profile {
    private int id;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private double remainingFunds;
    private double spentFunds;

    public Profile(int id, String name, String email, String phoneNumber, 
                  String profilePicture, double remainingFunds, double spentFunds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.remainingFunds = remainingFunds;
        this.spentFunds = spentFunds;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public double getRemainingFunds() {
        return remainingFunds;
    }

    public double getSpentFunds() {
        return spentFunds;
    }
}
