package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PaymentDao {
    
    @Insert
    void insert(PaymentEntity payment);
    
    @Update
    void update(PaymentEntity payment);
    
    @Delete
    void delete(PaymentEntity payment);
    
    @Query("SELECT * FROM payments WHERE id = :id")
    PaymentEntity getById(int id);
    
    @Query("SELECT * FROM payments WHERE payment_id = :paymentId")
    PaymentEntity getByPaymentId(String paymentId);
    
    @Query("SELECT * FROM payments ORDER BY created_at DESC")
    List<PaymentEntity> getAllPayments();
    
    @Query("SELECT * FROM payments WHERE status = :status ORDER BY created_at DESC")
    List<PaymentEntity> getPaymentsByStatus(String status);
    
    @Query("SELECT * FROM payments WHERE user_id = :userId ORDER BY created_at DESC")
    List<PaymentEntity> getPaymentsByUserId(int userId);
    
    @Query("SELECT * FROM payments WHERE user_email = :userEmail ORDER BY created_at DESC")
    List<PaymentEntity> getPaymentsByUserEmail(String userEmail);
    
    @Query("SELECT * FROM payments WHERE payment_method = :paymentMethod ORDER BY created_at DESC")
    List<PaymentEntity> getPaymentsByMethod(String paymentMethod);
    
    @Query("SELECT * FROM payments WHERE created_at BETWEEN :startTime AND :endTime ORDER BY created_at DESC")
    List<PaymentEntity> getPaymentsByDateRange(long startTime, long endTime);
    
    @Query("SELECT COUNT(*) FROM payments")
    int getTotalPaymentCount();
    
    @Query("SELECT COUNT(*) FROM payments WHERE status = :status")
    int getPaymentCountByStatus(String status);
    
    @Query("SELECT SUM(price) FROM payments WHERE status = 'COMPLETED'")
    Long getTotalRevenue();
    
    @Query("SELECT SUM(price) FROM payments WHERE status = 'COMPLETED' AND created_at BETWEEN :startTime AND :endTime")
    Long getRevenueByDateRange(long startTime, long endTime);
    
    @Query("SELECT package_name, COUNT(*) as count FROM payments WHERE status = 'COMPLETED' GROUP BY package_name ORDER BY count DESC")
    List<PackageStatistic> getPopularPackages();
    
    @Query("SELECT payment_method, COUNT(*) as count FROM payments GROUP BY payment_method ORDER BY count DESC")
    List<PaymentMethodStatistic> getPaymentMethodStats();
    
    @Query("DELETE FROM payments WHERE id = :id")
    void deleteById(int id);
    
    @Query("UPDATE payments SET status = :status WHERE id = :id")
    void updatePaymentStatus(int id, String status);
    
    @Query("UPDATE payments SET status = :status, completed_at = :completedAt WHERE id = :id")
    void updatePaymentStatusWithTime(int id, String status, long completedAt);
    
    // Inner classes for statistics
    class PackageStatistic {
        public String package_name;
        public int count;
    }
    
    class PaymentMethodStatistic {
        public String payment_method;
        public int count;
    }
}
