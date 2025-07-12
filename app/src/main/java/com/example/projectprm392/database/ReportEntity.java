package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import java.util.UUID;

@Entity(tableName = "reports")
public class ReportEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "ReportId")
    public String reportId;

    @ColumnInfo(name = "ReporterId")
    public String reporterId;

    @ColumnInfo(name = "TargetUserId")
    public String targetUserId;

    @ColumnInfo(name = "JobId")
    public String jobId;

    @ColumnInfo(name = "Content")
    public String content;

    @ColumnInfo(name = "CreatedAt")
    public long createdAt;

    @ColumnInfo(name = "Status")
    public String status;

    public ReportEntity(String reporterId, String targetUserId, String jobId, String content) {
        this.reportId = UUID.randomUUID().toString();
        this.reporterId = reporterId;
        this.targetUserId = targetUserId;
        this.jobId = jobId;
        this.content = content;
        this.createdAt = System.currentTimeMillis();
        this.status = "pending";
    }
}
