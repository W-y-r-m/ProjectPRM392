package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;

import java.math.BigDecimal;
import java.util.Date;

@Entity(tableName = "jobs",
        foreignKeys = @ForeignKey(entity = UserEntity.class,
                                 parentColumns = "id",
                                 childColumns = "user_id",
                                 onDelete = ForeignKey.CASCADE))
public class JobEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "job_id")
    private String jobId;
    
    @ColumnInfo(name = "user_id")
    private int userId;
    
    @ColumnInfo(name = "title")
    private String title;
    
    @ColumnInfo(name = "description")
    private String description;
    
    @ColumnInfo(name = "salary")
    private String salary;
    
    @ColumnInfo(name = "location")
    private String location;
    
    @ColumnInfo(name = "job_type")
    private String jobType;
    
    @ColumnInfo(name = "experience_level")
    private String experienceLevel;
    
    @ColumnInfo(name = "needed_amount")
    private int neededAmount;
    
    @ColumnInfo(name = "working_time")
    private String workingTime;
    
    @ColumnInfo(name = "start_date")
    private Date startDate;
    
    @ColumnInfo(name = "end_date")
    private Date endDate;
    
    @ColumnInfo(name = "location_latitude")
    private Double locationLatitude;
    
    @ColumnInfo(name = "location_longitude")
    private Double locationLongitude;
    
    @ColumnInfo(name = "post_type")
    private String postType; // JOB_SEEKING or JOB_POSTING
    
    @ColumnInfo(name = "status")
    private String status;
    
    @ColumnInfo(name = "created_at")
    private Date createdAt;
    
    @ColumnInfo(name = "is_active")
    private Boolean isActive;

    // Constructors
    public JobEntity() {}

    public JobEntity(String jobId, int userId, String title, String description, 
                    String salary, String location, String jobType, String experienceLevel,
                    int neededAmount, String workingTime, Date startDate, Date endDate,
                    Double locationLatitude, Double locationLongitude, String postType, 
                    String status, Date createdAt, Boolean isActive) {
        this.jobId = jobId;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.location = location;
        this.jobType = jobType;
        this.experienceLevel = experienceLevel;
        this.neededAmount = neededAmount;
        this.workingTime = workingTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.postType = postType;
        this.status = status;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationName() {
        return location;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public int getNeededAmount() {
        return neededAmount;
    }

    public void setNeededAmount(int neededAmount) {
        this.neededAmount = neededAmount;
    }

    public String getWorkingTime() {
        return workingTime;
    }

    public void setWorkingTime(String workingTime) {
        this.workingTime = workingTime;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
