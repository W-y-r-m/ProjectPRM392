package com.example.projectprm392.models;

import java.util.Date;
import java.util.UUID;

public class User {
    private UUID userId;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private Boolean otpVerified;
    private Boolean gender;
    private String role;
    private String levelOfViolation;
    private String description;
    private Integer postQuota;
    private Double currentLatitude;
    private Double currentLongitude;
    private String address;
    private Date createdAt;
    private Boolean isActive;
    private Boolean isVerified;
    private String verificationCode;
    private Date verificationCodeExpiresAt;

    // Constructors
    public User() {
        this.userId = UUID.randomUUID();
        this.createdAt = new Date();
        this.isActive = true;
        this.levelOfViolation = "NONE";

        this.postQuota = 0; // Default post quota changed to 0

    }

    public User(String email, String password, String fullName, String role) {
        this();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getOtpVerified() {
        return otpVerified;
    }

    public void setOtpVerified(Boolean otpVerified) {
        this.otpVerified = otpVerified;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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



    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    // Alias methods for compatibility
    public void setVerified(boolean verified) {
        this.isVerified = verified;
    }

    public boolean isVerified() {
        return isVerified;
    }

    // Utility methods cho post quota
    public boolean needsTopUp() {
        return postQuota != null && postQuota >= 20; // Cần nạp tiền khi >= 20
    }

    public boolean canPost() {
        return postQuota != null && postQuota < 20; // Có thể đăng khi < 20
    }

    public void usePostQuota() {
        if (postQuota != null && postQuota < 20) {
            postQuota++; // Tăng quota khi đăng tin (đếm lên)
        }
    }

    public void addPostQuota(int amount) {
        if (postQuota == null) {
            postQuota = amount;
        } else {
            postQuota += amount;
        }
    }
    
    public void reducePostQuotaByPayment(int amount) {
        if (postQuota == null) {
            postQuota = 0;
        } else {
            postQuota = Math.max(0, postQuota - amount); // Trừ quota khi nạp tiền
        }
    }

    public String getPostQuotaStatus() {
        if (postQuota == null)
            return "Chưa xác định";
        if (postQuota >= 20)
            return "Cần nạp tiền";
        if (postQuota > 10)
            return "Bình thường";
        if (postQuota > 5)
            return "Sắp hết quota";
        if (postQuota > 0)
            return "Quota thấp";
        return "Hết quota";
    }

    // Methods cho profile update
    public boolean isPostQuotaEditable() {
        return false; // Không cho phép edit postQuota
    }

    public User updateProfile(String fullName, String phoneNumber, String description, Boolean gender) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
        }
        // Cập nhật phone number (cho phép rỗng)
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber.trim().isEmpty() ? null : phoneNumber.trim();
        }
        // Cập nhật description (cho phép rỗng)
        if (description != null) {
            this.description = description.trim().isEmpty() ? null : description.trim();
        }
        if (gender != null) {
            this.gender = gender;
        }
        return this;
    }

    public boolean validateProfileUpdate(String fullName, String phoneNumber) {
        // Validate fullName
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }

        // Validate phoneNumber - sử dụng method từ UserService
        return true; // Để UserService handle phone validation
    }

    // Copy constructor để tránh reference issues
    public User createCopy() {
        User copy = new User();
        copy.userId = this.userId;
        copy.email = this.email;
        copy.password = this.password;
        copy.fullName = this.fullName;
        copy.phoneNumber = this.phoneNumber;
        copy.otpVerified = this.otpVerified;
        copy.gender = this.gender;
        copy.role = this.role;
        copy.levelOfViolation = this.levelOfViolation;
        copy.description = this.description;
        copy.postQuota = this.postQuota;
        copy.currentLatitude = this.currentLatitude;
        copy.currentLongitude = this.currentLongitude;
        copy.address = this.address;
        copy.createdAt = this.createdAt;
        copy.isActive = this.isActive;
        return copy;
    }
}
