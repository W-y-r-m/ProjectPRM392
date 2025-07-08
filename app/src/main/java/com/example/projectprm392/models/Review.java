package com.example.projectprm392.models;

import java.sql.Timestamp;
import java.util.UUID;

public class Review {
    private UUID reviewId;
    private UUID reviewerId;
    private UUID revieweeId;
    private UUID jobId;
    private Integer rating;
    private String comment;
    private Timestamp createdAt;

    // Constructors
    public Review() {
        this.reviewId = UUID.randomUUID();
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Review(UUID reviewerId, UUID revieweeId, UUID jobId, Integer rating, String comment) {
        this();
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.jobId = jobId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public UUID getReviewId() {
        return reviewId;
    }

    public void setReviewId(UUID reviewId) {
        this.reviewId = reviewId;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(UUID reviewerId) {
        this.reviewerId = reviewerId;
    }

    public UUID getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(UUID revieweeId) {
        this.revieweeId = revieweeId;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        } else {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
