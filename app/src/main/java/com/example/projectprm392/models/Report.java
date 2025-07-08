package com.example.projectprm392.models;

import java.sql.Timestamp;
import java.util.UUID;

public class Report {
    private UUID reportId;
    private UUID reporterId;
    private UUID targetUserId;
    private UUID jobId;
    private String content;
    private Timestamp createdAt;

    // Constructors
    public Report() {
        this.reportId = UUID.randomUUID();
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Report(UUID reporterId, UUID targetUserId, UUID jobId, String content) {
        this();
        this.reporterId = reporterId;
        this.targetUserId = targetUserId;
        this.jobId = jobId;
        this.content = content;
    }

    // Getters and Setters
    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
