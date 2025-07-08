package com.example.projectprm392.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class Payment {
    private UUID paymentId;
    private UUID userId;
    private Integer slotAmount;
    private BigDecimal amount;
    private PaymentStatus status;
    private String note;
    private Timestamp createdAt;
    private Timestamp completedAt;

    public enum PaymentStatus {
        PENDING("pending"),
        COMPLETED("completed"),
        CANCELLED("cancelled");

        private final String value;

        PaymentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructors
    public Payment() {
        this.paymentId = UUID.randomUUID();
        this.status = PaymentStatus.PENDING;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public Payment(UUID userId, Integer slotAmount, BigDecimal amount) {
        this();
        this.userId = userId;
        this.slotAmount = slotAmount;
        this.amount = amount;
    }

    // Getters and Setters
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getSlotAmount() {
        return slotAmount;
    }

    public void setSlotAmount(Integer slotAmount) {
        this.slotAmount = slotAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
