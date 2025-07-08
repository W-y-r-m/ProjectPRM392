package com.example.projectprm392.models;

import java.sql.Timestamp;
import java.util.UUID;

public class Application {
    private UUID applicationId;
    private UUID jobId;
    private UUID userId;
    private String message;
    private String otherFileUrl;
    private ApplicationStatus status;
    private String reply;
    private Timestamp appliedAt;

    public enum ApplicationStatus {
        PENDING("pending"),
        ACCEPTED("accepted"),
        REJECTED("rejected"),
        COMPLETED("completed");

        private final String value;

        ApplicationStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructors
    public Application() {
        this.applicationId = UUID.randomUUID();
        this.status = ApplicationStatus.PENDING;
        this.appliedAt = new Timestamp(System.currentTimeMillis());
    }

    public Application(UUID jobId, UUID userId, String message) {
        this();
        this.jobId = jobId;
        this.userId = userId;
        this.message = message;
    }

    // Getters and Setters
    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOtherFileUrl() {
        return otherFileUrl;
    }

    public void setOtherFileUrl(String otherFileUrl) {
        this.otherFileUrl = otherFileUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }
}
