package com.example.projectprm392.services;

import com.example.projectprm392.models.User;

/**
 * Service để xử lý Post Quota và Payment
 */
public class QuotaService {
    
    /**
     * Kiểm tra xem user có cần nạp tiền không
     */
    public static boolean needsTopUp(User user) {
        return user != null && user.needsTopUp();
    }
    
    /**
     * Kiểm tra xem user có thể đăng tin không
     */
    public static boolean canPost(User user) {
        return user != null && user.canPost();
    }
    
    /**
     * Lấy thông báo về trạng thái quota
     */
    public static String getQuotaMessage(User user) {
        if (user == null || user.getPostQuota() == null) {
            return "Không thể xác định trạng thái quota";
        }
        
        int quota = user.getPostQuota();
        
        if (quota >= 20) {
            return "⚠️ Bạn cần nạp tiền để tiếp tục đăng tin. Quota hiện tại: " + quota;
        } else if (quota > 10) {
            return "✅ Bạn có thể đăng tin bình thường. Quota còn lại: " + quota;
        } else if (quota > 5) {
            return "⚡ Quota sắp hết. Còn lại: " + quota + " lần đăng";
        } else if (quota > 0) {
            return "🔴 Quota thấp. Chỉ còn: " + quota + " lần đăng";
        } else {
            return "❌ Hết quota. Vui lòng nạp thêm để đăng tin";
        }
    }
    
    /**
     * Xử lý việc đăng tin (trừ quota)
     */
    public static PostResult processPost(User user) {
        if (user == null) {
            return new PostResult(false, "User không hợp lệ");
        }
        
        if (user.getPostQuota() == null) {
            return new PostResult(false, "Quota chưa được khởi tạo");
        }
        
        if (user.getPostQuota() >= 20) {
            return new PostResult(false, "Cần nạp tiền trước khi đăng tin");
        }
        
        if (user.getPostQuota() <= 0) {
            return new PostResult(false, "Hết quota. Vui lòng nạp thêm");
        }
        
        // Trừ quota
        user.usePostQuota();
        return new PostResult(true, "Đăng tin thành công. Quota còn lại: " + user.getPostQuota());
    }
    
    /**
     * Nạp quota cho user
     */
    public static TopUpResult topUpQuota(User user, int amount, String paymentMethod) {
        if (user == null) {
            return new TopUpResult(false, "User không hợp lệ", 0);
        }
        
        if (amount <= 0) {
            return new TopUpResult(false, "Số lượng quota phải lớn hơn 0", user.getPostQuota());
        }
        
        int oldQuota = user.getPostQuota() != null ? user.getPostQuota() : 0;
        user.addPostQuota(amount);
        
        String message = String.format("Nạp thành công %d quota bằng %s. Quota: %d → %d", 
                                       amount, paymentMethod, oldQuota, user.getPostQuota());
        
        return new TopUpResult(true, message, user.getPostQuota());
    }
    
    /**
     * Tính toán giá nạp quota
     */
    public static int calculatePrice(int quotaAmount) {
        // Giá mẫu: 1000 VND / 1 quota
        return quotaAmount * 1000;
    }
    
    /**
     * Lấy các gói nạp quota có sẵn
     */
    public static QuotaPackage[] getAvailablePackages() {
        return new QuotaPackage[] {
            new QuotaPackage(5, 5000, "Gói cơ bản"),
            new QuotaPackage(10, 9000, "Gói phổ biến"),
            new QuotaPackage(20, 17000, "Gói tiết kiệm"),
            new QuotaPackage(50, 40000, "Gói doanh nghiệp")
        };
    }
    
    // Inner classes for results
    public static class PostResult {
        public boolean success;
        public String message;
        
        public PostResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    public static class TopUpResult {
        public boolean success;
        public String message;
        public int newQuota;
        
        public TopUpResult(boolean success, String message, int newQuota) {
            this.success = success;
            this.message = message;
            this.newQuota = newQuota;
        }
    }
    
    public static class QuotaPackage {
        public int quotaAmount;
        public int price;
        public String name;
        
        public QuotaPackage(int quotaAmount, int price, String name) {
            this.quotaAmount = quotaAmount;
            this.price = price;
            this.name = name;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %d quota - %,d VND", name, quotaAmount, price);
        }
    }
}
