package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.ColumnInfo;

import java.math.BigDecimal;

@Entity(tableName = "payments",
        foreignKeys = @ForeignKey(entity = UserEntity.class,
                                parentColumns = "id",
                                childColumns = "user_id",
                                onDelete = ForeignKey.CASCADE))
public class PaymentEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "payment_id")
    private String paymentId;
    
    @ColumnInfo(name = "user_id")
    private int userId;
    
    @ColumnInfo(name = "user_email")
    private String userEmail;
    
    @ColumnInfo(name = "user_name")
    private String userName;
    
    @ColumnInfo(name = "quota_amount")
    private int quotaAmount;
    
    @ColumnInfo(name = "price")
    private int price; // VND
    
    @ColumnInfo(name = "package_name")
    private String packageName;
    
    @ColumnInfo(name = "payment_method")
    private String paymentMethod; // MOMO, BANKING, VNPAY, etc.
    
    @ColumnInfo(name = "status")
    private String status; // PENDING, COMPLETED, CANCELLED, FAILED
    
    @ColumnInfo(name = "transaction_id")
    private String transactionId;
    
    @ColumnInfo(name = "note")
    private String note;
    
    @ColumnInfo(name = "created_at")
    private long createdAt;
    
    @ColumnInfo(name = "completed_at")
    private Long completedAt;

    // Constructors
    public PaymentEntity() {
        this.createdAt = System.currentTimeMillis();
        this.status = "PENDING";
    }

    public PaymentEntity(String paymentId, int userId, String userEmail, String userName,
                        int quotaAmount, int price, String packageName, String paymentMethod) {
        this();
        this.paymentId = paymentId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.quotaAmount = quotaAmount;
        this.price = price;
        this.packageName = packageName;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getQuotaAmount() {
        return quotaAmount;
    }

    public void setQuotaAmount(int quotaAmount) {
        this.quotaAmount = quotaAmount;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equals(status) && this.completedAt == null) {
            this.completedAt = System.currentTimeMillis();
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
    }

    // Helper methods
    public String getFormattedPrice() {
        return String.format("%,d VND", price);
    }

    public String getStatusText() {
        switch (status) {
            case "PENDING": return "Đang chờ";
            case "COMPLETED": return "Hoàn thành";
            case "CANCELLED": return "Đã hủy";
            case "FAILED": return "Thất bại";
            default: return status;
        }
    }

    public String getPaymentMethodText() {
        switch (paymentMethod) {
            case "MOMO": return "Ví MoMo";
            case "BANKING": return "Chuyển khoản";
            case "VNPAY": return "VNPay";
            case "CREDIT_CARD": return "Thẻ tín dụng";
            default: return paymentMethod;
        }
    }
}
