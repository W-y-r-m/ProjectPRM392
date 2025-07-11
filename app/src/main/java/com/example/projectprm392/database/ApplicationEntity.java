package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import androidx.room.ForeignKey;

import java.util.UUID;

@Entity(tableName = "applications", foreignKeys = {
        @ForeignKey(entity = JobEntity.class, parentColumns = "id", childColumns = "jobId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE)
})
public class ApplicationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "applicationId")
    private String applicationId;

    @ColumnInfo(name = "jobId")
    private int jobId;

    @ColumnInfo(name = "userId")
    private int userId;

    @ColumnInfo(name = "message")
    private String message;

    @ColumnInfo(name = "otherFileUrl")
    private String otherFileUrl;

    @ColumnInfo(name = "status")
    private String status = "pending";

    @ColumnInfo(name = "reply")
    private String reply;

    @ColumnInfo(name = "appliedAt")
    private long appliedAt;

    // Default constructor required by Room
    public ApplicationEntity() {
        this.applicationId = UUID.randomUUID().toString();
        this.status = "PENDING";
        this.appliedAt = System.currentTimeMillis();
    }

    public ApplicationEntity(int id, @NonNull String applicationId, int jobId, int userId, String message,
            String otherFileUrl, String status, String reply, long appliedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.userId = userId;
        this.message = message;
        this.otherFileUrl = otherFileUrl;
        this.status = status;
        this.reply = reply;
        this.appliedAt = appliedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(@NonNull String applicationId) {
        this.applicationId = applicationId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public long getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(long appliedAt) {
        this.appliedAt = appliedAt;
    }
}
