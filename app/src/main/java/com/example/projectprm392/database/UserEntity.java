package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

import java.util.Date;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "user_id")
    private String userId;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "password")
    private String password;
    
    @ColumnInfo(name = "full_name")
    private String fullName;
    
    @ColumnInfo(name = "phone_number")
    private String phoneNumber;
    
    @ColumnInfo(name = "gender")
    private Boolean gender;
    
    @ColumnInfo(name = "role")
    private String role;
    
    @ColumnInfo(name = "level_of_violation")
    private String levelOfViolation;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "post_quota")
    private Integer postQuota;
    
    @ColumnInfo(name = "current_latitude")
    private Double currentLatitude;
    
    @ColumnInfo(name = "current_longitude")
    private Double currentLongitude;
    
    @ColumnInfo(name = "created_at")
    private Date createdAt;
    
    @ColumnInfo(name = "is_active")
    private Boolean isActive;
    
    @ColumnInfo(name = "is_verified")
    private boolean isVerified;
    
    @ColumnInfo(name = "verification_code")
    private String verificationCode;
    
    @ColumnInfo(name = "verification_code_expires_at")
    private Date verificationCodeExpiresAt;

    // Constructors
    public UserEntity() {}

    public UserEntity(String userId, String email, String password, String fullName, 
                     String phoneNumber, Boolean gender, String role, String levelOfViolation, String description, 
                     Integer postQuota, Double currentLatitude, Double currentLongitude, 
                     Date createdAt, Boolean isActive, boolean isVerified, String verificationCode, 
                     Date verificationCodeExpiresAt) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.role = role;
        this.levelOfViolation = levelOfViolation;
        this.description = description;
        this.postQuota = postQuota;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.isVerified = isVerified;
        this.verificationCode = verificationCode;
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public Date getVerificationCodeExpiresAt() {
        return verificationCodeExpiresAt;
    }

    public void setVerificationCodeExpiresAt(Date verificationCodeExpiresAt) {
        this.verificationCodeExpiresAt = verificationCodeExpiresAt;
    }
}
