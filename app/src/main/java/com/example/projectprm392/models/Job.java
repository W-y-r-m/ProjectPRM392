package com.example.projectprm392.models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

public class Job {
    private UUID jobId;
    private UUID userId;
    private String title;
    private String description;
    private BigDecimal salary;
    private SalaryUnit salaryUnit;
    private Time startTime;
    private Time endTime;
    private Date workingDate;
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer neededAmount;
    private JobStatus status;
    private Timestamp createdAt;
    private Boolean isDeleted;

    public enum SalaryUnit {
        HOUR("hour"),
        DAY("day"),
        PACKAGE("package");

        private final String value;

        SalaryUnit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobStatus {
        OPEN("open"),
        CLOSED("closed"),
        IN_PROGRESS("in_progress");

        private final String value;

        JobStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructors
    public Job() {
        this.jobId = UUID.randomUUID();
        this.status = JobStatus.OPEN;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isDeleted = false;
    }

    public Job(UUID userId, String title, String description, BigDecimal salary, SalaryUnit salaryUnit) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.salaryUnit = salaryUnit;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public SalaryUnit getSalaryUnit() {
        return salaryUnit;
    }

    public void setSalaryUnit(SalaryUnit salaryUnit) {
        this.salaryUnit = salaryUnit;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Date getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(Date workingDate) {
        this.workingDate = workingDate;
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

    public Integer getNeededAmount() {
        return neededAmount;
    }

    public void setNeededAmount(Integer neededAmount) {
        this.neededAmount = neededAmount;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
