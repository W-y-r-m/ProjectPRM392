package com.example.projectprm392.models;

import java.util.Date;
import java.util.UUID;

public class User {
    private UUID userId;
    private String phoneNumber;
    private String fullName;
    private String email;
    private String password;
    private Boolean gender;
    private String role;
    private String levelOfViolation;
    private String description;
    private Integer postQuota;
    private Double currentLatitude;
    private Double currentLongitude;
    private Boolean otpVerified;
    private Date createdAt;
    private Boolean isActive;

    // Constructors
    public User() {
        this.userId = UUID.randomUUID();
        this.createdAt = new Date();
        this.isActive = true;
        this.levelOfViolation = "NONE";
        this.postQuota = 10; // Default post quota
        this.otpVerified = false;
    }

    public User(String phoneNumber, String fullName, String role) {
        this();
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.role = role;
    }

    public User(String phoneNumber, String email, String password, String fullName, String role) {
        this();
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLevelOfViolation() {
        return levelOfViolation;
    }

    public void setLevelOfViolation(String levelOfViolation) {
        this.levelOfViolation = levelOfViolation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPostQuota() {
        return postQuota;
    }

    public void setPostQuota(Integer postQuota) {
        this.postQuota = postQuota;
    }

    public Double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(Double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public Double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(Double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Boolean getOtpVerified() {
        return otpVerified;
    }

    public void setOtpVerified(Boolean otpVerified) {
        this.otpVerified = otpVerified;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
