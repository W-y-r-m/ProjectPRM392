package com.example.projectprm392.models;

import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

public class Schedule {
    private UUID scheduleId;
    private UUID jobId;
    private UUID userId;
    private Date availableDate;
    private Time startTime;
    private Time endTime;

    // Constructors
    public Schedule() {
        this.scheduleId = UUID.randomUUID();
    }

    public Schedule(UUID jobId, UUID userId, Date availableDate, Time startTime, Time endTime) {
        this();
        this.jobId = jobId;
        this.userId = userId;
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
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

    public Date getAvailableDate() {
        return availableDate;
    }

    public void setAvailableDate(Date availableDate) {
        this.availableDate = availableDate;
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
}
