package com.example.projectprm392.models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

/**
 * Model cho việc tạo job posting (đăng tin tuyển dụng)
 * Dựa trên dữ liệu từ bảng Job với đầy đủ thông tin
 * Vị trí có thể chọn trên Google Maps hoặc lấy vị trí hiện tại
 */
public class JobPostingRequest {
    private String title;
    private String description;
    private BigDecimal salary;
    private Job.SalaryUnit salaryUnit;
    private Time startTime;
    private Time endTime;
    private Date workingDate;
    private Double locationLatitude;
    private Double locationLongitude;
    private String locationAddress;
    private Integer neededAmount;
    private Job.JobCategory category;
    private Job.JobLevel level;
    private boolean useCurrentLocation;

    public JobPostingRequest() {
        this.useCurrentLocation = false;
    }

    public JobPostingRequest(String title, String description, BigDecimal salary, 
                           Job.SalaryUnit salaryUnit, Job.JobCategory category, Job.JobLevel level) {
        this();
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.salaryUnit = salaryUnit;
        this.category = category;
        this.level = level;
    }

    // Convert to Job object
    public Job toJob(UUID userId) {
        Job job = new Job(userId, title, description, salary, salaryUnit, category, level, 
                         startTime, endTime, workingDate, locationLatitude, locationLongitude, locationAddress);
        job.setNeededAmount(neededAmount);
        return job;
    }

    // Getters and Setters
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

    public Job.SalaryUnit getSalaryUnit() {
        return salaryUnit;
    }

    public void setSalaryUnit(Job.SalaryUnit salaryUnit) {
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

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Integer getNeededAmount() {
        return neededAmount;
    }

    public void setNeededAmount(Integer neededAmount) {
        this.neededAmount = neededAmount;
    }

    public Job.JobCategory getCategory() {
        return category;
    }

    public void setCategory(Job.JobCategory category) {
        this.category = category;
    }

    public Job.JobLevel getLevel() {
        return level;
    }

    public void setLevel(Job.JobLevel level) {
        this.level = level;
    }

    public boolean isUseCurrentLocation() {
        return useCurrentLocation;
    }

    public void setUseCurrentLocation(boolean useCurrentLocation) {
        this.useCurrentLocation = useCurrentLocation;
    }

    // Validation
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               salary != null && salary.compareTo(BigDecimal.ZERO) > 0 &&
               salaryUnit != null &&
               category != null &&
               level != null &&
               ((locationLatitude != null && locationLongitude != null) || useCurrentLocation);
    }

    // Utility methods
    public String getFormattedSalary() {
        if (salary == null) return "";
        return salary.toString() + " VND/" + (salaryUnit != null ? salaryUnit.getValue() : "");
    }

    public String getFormattedWorkingTime() {
        if (startTime == null || endTime == null) return "";
        return startTime.toString() + " - " + endTime.toString();
    }
}
