package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import androidx.room.ForeignKey;

import java.util.UUID;

@Entity(tableName = "chat_messages", foreignKeys = {
        @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "senderId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = UserEntity.class, parentColumns = "id", childColumns = "receiverId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = JobEntity.class, parentColumns = "id", childColumns = "jobId", onDelete = ForeignKey.CASCADE)
})
public class ChatEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "messageId")
    private String messageId;

    @ColumnInfo(name = "senderId")
    private int senderId;

    @ColumnInfo(name = "receiverId")
    private int receiverId;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "sentAt")
    private long sentAt;

    @ColumnInfo(name = "jobId")
    private int jobId;

    @ColumnInfo(name = "isRead")
    private boolean isRead = false;

    // Default constructor
    public ChatEntity() {
        this.messageId = UUID.randomUUID().toString();
        this.sentAt = System.currentTimeMillis();
    }

    public ChatEntity(int id, @NonNull String messageId, int senderId, int receiverId, 
                     String content, long sentAt, int jobId, boolean isRead) {
        this.id = id;
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = sentAt;
        this.jobId = jobId;
        this.isRead = isRead;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
