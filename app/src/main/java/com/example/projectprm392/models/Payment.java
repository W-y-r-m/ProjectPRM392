package com.example.projectprm392.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class Payment {
    private UUID paymentId;
    private UUID userId;
    private String orderCode;
    private Integer slotAmount;
    private BigDecimal amount;
    private PaymentStatus status;
    private String note;
    private Timestamp createdAt;
    private Timestamp completedAt;
    
    // PayOS specific fields
    private String payosOrderId;
    private String qrCodeUrl;
    private String transactionId;
    private String paymentMethod;
    private String errorMessage; // For storing error details when PayOS API fails

    public enum PaymentStatus {
        PENDING("pending"),
        COMPLETED("completed"),
        CANCELLED("cancelled"),
        FAILED("failed");

        private final String value;

        PaymentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
        
        public String getDisplayText() {
            switch (this) {
                case PENDING:
                    return "Đang xử lý";
                case COMPLETED:
                    return "Thành công";
                case CANCELLED:
                    return "Đã hủy";
                case FAILED:
                    return "Thất bại";
                default:
                    return "Không xác định";
            }
        }
    }

    // Constructors
    public Payment() {
        this.paymentId = UUID.randomUUID();
        this.status = PaymentStatus.PENDING;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.paymentMethod = "PAYOS";
        this.orderCode = generateOrderCode();
    }

    public Payment(UUID userId, Integer slotAmount, BigDecimal amount, String note) {
        this();
        this.userId = userId;
        this.slotAmount = slotAmount;
        this.amount = amount;
        this.note = note;
    }
    
    // Generate unique order code for PayOS
    private String generateOrderCode() {
        return "ORDER_" + System.currentTimeMillis();
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
    
    // PayOS specific getters and setters
    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getPayosOrderId() {
        return payosOrderId;
    }

    public void setPayosOrderId(String payosOrderId) {
        this.payosOrderId = payosOrderId;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Utility methods
    public boolean isSuccessful() {
        return status == PaymentStatus.COMPLETED;
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public boolean isFailed() {
        return status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED;
    }

    public String getFormattedAmount() {
        if (amount == null) return "0đ";
        return String.format("%,.0fđ", amount);
    }
    
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = new Timestamp(System.currentTimeMillis());
    }
    
    public void markAsFailed() {
        this.status = PaymentStatus.FAILED;
        this.completedAt = new Timestamp(System.currentTimeMillis());
    }
}
