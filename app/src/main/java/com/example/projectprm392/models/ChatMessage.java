package com.example.projectprm392.models;

import java.sql.Timestamp;
import java.util.UUID;

public class ChatMessage {
    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private Timestamp sentAt;
    private UUID jobId;

    // Constructors
    public ChatMessage() {
        this.messageId = UUID.randomUUID();
        this.sentAt = new Timestamp(System.currentTimeMillis());
    }

    public ChatMessage(UUID senderId, UUID receiverId, String content, UUID jobId) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.jobId = jobId;
    }

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }
}
