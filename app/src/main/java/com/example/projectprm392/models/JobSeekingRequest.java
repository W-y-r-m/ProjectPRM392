package com.example.projectprm392.models;

import java.util.UUID;

/**
 * Model cho việc tạo job seeking (đăng tin tìm việc)
 * Chỉ cần title và description
 * Vị trí sẽ được cập nhật từ Google Maps
 */
public class JobSeekingRequest {
    private String title;
    private String description;
    private Double locationLatitude;
    private Double locationLongitude;
    private String locationAddress;

    public JobSeekingRequest() {
    }

    public JobSeekingRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // Convert to Job object
    public Job toJob(UUID userId) {
        Job job = new Job(userId, title, description, null, Job.JobType.JOB_SEEKING);
        job.setLocationLatitude(locationLatitude);
        job.setLocationLongitude(locationLongitude);
        job.setLocationAddress(locationAddress);
        job.setNeededAmount(1); // Mặc định là 1 cho job seeking
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

    // Validation
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               description != null && !description.trim().isEmpty();
    }
}
