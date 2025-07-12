package com.example.projectprm392.examples;

import android.content.Context;
import com.example.projectprm392.models.User;
import com.example.projectprm392.models.JobSeekingRequest;
import com.example.projectprm392.models.JobPostingRequest;
import com.example.projectprm392.models.Job;
import com.example.projectprm392.services.JobService;
import com.example.projectprm392.services.QuotaService;
import java.math.BigDecimal;

/**
 * Ví dụ về cách sử dụng Job system với quota checking
 */
public class QuotaJobExample {
    
    public static void demonstrateQuotaSystem(Context context) {
        System.out.println("=== VÍ DỤ HỆ THỐNG QUOTA ===");
        
        // Tạo user với quota khác nhau để test
        User user1 = new User("user1@test.com", "password", "User 1", "user");
        user1.setPostQuota(15); // Có thể đăng tin
        
        User user2 = new User("user2@test.com", "password", "User 2", "user");
        user2.setPostQuota(25); // Cần nạp tiền
        
        User user3 = new User("user3@test.com", "password", "User 3", "user");
        user3.setPostQuota(3); // Quota thấp
        
        testUserQuota(context, user1);
        testUserQuota(context, user2);
        testUserQuota(context, user3);
    }
    
    private static void testUserQuota(Context context, User user) {
        System.out.println("\n--- TEST USER: " + user.getFullName() + " ---");
        System.out.println("Quota hiện tại: " + user.getPostQuota());
        System.out.println("Trạng thái: " + QuotaService.getQuotaMessage(user));
        
        JobService jobService = new JobService(context);
        
        // Test tạo job seeking
        JobSeekingRequest seekingRequest = new JobSeekingRequest();
        seekingRequest.setTitle("Tìm việc test - " + user.getFullName());
        seekingRequest.setDescription("Mô tả test");
        
        JobService.JobCreationResult result = jobService.createJobSeekingWithQuota(
            seekingRequest, user, true
        );
        
        System.out.println("Kết quả đăng tin: " + (result.success ? "THÀNH CÔNG" : "THẤT BẠI"));
        System.out.println("Thông báo: " + result.message);
        
        if (result.success) {
            System.out.println("Job đã tạo: " + result.job.getTitle());
        }
        
        System.out.println("Quota sau khi đăng: " + user.getPostQuota());
    }
    
    public static void demonstrateTopUpSystem() {
        System.out.println("\n=== VÍ DỤ HỆ THỐNG NẠP TIỀN ===");
        
        User user = new User("topup@test.com", "password", "TopUp User", "user");
        user.setPostQuota(25); // Cần nạp tiền
        
        System.out.println("Trước khi nạp:");
        System.out.println("Quota: " + user.getPostQuota());
        System.out.println("Cần nạp tiền: " + (user.needsTopUp() ? "CÓ" : "KHÔNG"));
        System.out.println("Có thể đăng tin: " + (user.canPost() ? "CÓ" : "KHÔNG"));
        
        // Hiển thị các gói nạp có sẵn
        System.out.println("\nCác gói nạp có sẵn:");
        QuotaService.QuotaPackage[] packages = QuotaService.getAvailablePackages();
        for (int i = 0; i < packages.length; i++) {
            System.out.println((i + 1) + ". " + packages[i]);
        }
        
        // Nạp 10 quota
        QuotaService.TopUpResult topUpResult = QuotaService.topUpQuota(user, 10, "Visa");
        
        System.out.println("\nSau khi nạp:");
        System.out.println("Kết quả: " + (topUpResult.success ? "THÀNH CÔNG" : "THẤT BẠI"));
        System.out.println("Thông báo: " + topUpResult.message);
        System.out.println("Quota mới: " + topUpResult.newQuota);
        System.out.println("Cần nạp tiền: " + (user.needsTopUp() ? "CÓ" : "KHÔNG"));
        System.out.println("Có thể đăng tin: " + (user.canPost() ? "CÓ" : "KHÔNG"));
    }
    
    public static void demonstrateQuotaScenarios(Context context) {
        System.out.println("\n=== CÁC TÌNH HUỐNG QUOTA ===");
        
        JobService jobService = new JobService(context);
        
        // Scenario 1: User bình thường (quota <= 20)
        User normalUser = new User("normal@test.com", "password", "Normal User", "user");
        normalUser.setPostQuota(15);
        testPostingScenario(jobService, normalUser, "User bình thường");
        
        // Scenario 2: User cần nạp tiền (quota > 20)
        User topUpUser = new User("needtopup@test.com", "password", "Need TopUp User", "user");
        topUpUser.setPostQuota(25);
        testPostingScenario(jobService, topUpUser, "User cần nạp tiền");
        
        // Scenario 3: User hết quota
        User emptyUser = new User("empty@test.com", "password", "Empty User", "user");
        emptyUser.setPostQuota(0);
        testPostingScenario(jobService, emptyUser, "User hết quota");
        
        // Scenario 4: User quota thấp
        User lowUser = new User("low@test.com", "password", "Low User", "user");
        lowUser.setPostQuota(2);
        testPostingScenario(jobService, lowUser, "User quota thấp");
    }
    
    private static void testPostingScenario(JobService jobService, User user, String scenarioName) {
        System.out.println("\n--- " + scenarioName.toUpperCase() + " ---");
        System.out.println("Quota: " + user.getPostQuota());
        System.out.println("Trạng thái: " + user.getPostQuotaStatus());
        
        JobSeekingRequest request = new JobSeekingRequest();
        request.setTitle("Test job - " + scenarioName);
        request.setDescription("Test description");
        
        JobService.JobCreationResult result = jobService.createJobSeekingWithQuota(
            request, user, true
        );
        
        System.out.println("Có thể đăng tin: " + (result.success ? "CÓ" : "KHÔNG"));
        System.out.println("Thông báo: " + result.message);
        
        if (result.success) {
            System.out.println("Quota còn lại: " + user.getPostQuota());
        }
    }
    
    public static void demonstrateJobSeekingFlow(Context context) {
        System.out.println("\n=== FLOW ĐĂNG TIN TÌM VIỆC ===");
        
        User user = new User("jobseeker@test.com", "password", "Job Seeker", "user");
        user.setPostQuota(5); // Quota bình thường
        
        JobService jobService = new JobService(context);
        
        // Tạo job seeking request - CHỈ CẦN 2 TRƯỜNG
        JobSeekingRequest request = new JobSeekingRequest();
        request.setTitle("Tìm việc Developer Android");
        request.setDescription("Có kinh nghiệm 3 năm phát triển ứng dụng Android với Java và Kotlin");
        // KHÔNG CẦN SET neededAmount nữa!
        
        System.out.println("Thông tin đăng tin tìm việc:");
        System.out.println("- Tiêu đề: " + request.getTitle());
        System.out.println("- Mô tả: " + request.getDescription());
        System.out.println("- User quota: " + user.getPostQuota());
        System.out.println("- Có thể đăng tin: " + (user.canPost() ? "CÓ" : "KHÔNG"));
        
        if (user.needsTopUp()) {
            System.out.println("⚠️ " + QuotaService.getQuotaMessage(user));
            return;
        }
        
        JobService.JobCreationResult result = jobService.createJobSeekingWithQuota(
            request, user, true // true = sử dụng vị trí hiện tại
        );
        
        System.out.println("\nKết quả:");
        System.out.println("Trạng thái: " + (result.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Thông báo: " + result.message);
        
        if (result.success && result.job != null) {
            Job job = result.job;
            System.out.println("\nThông tin job đã tạo:");
            System.out.println("- ID: " + job.getJobId());
            System.out.println("- Tiêu đề: " + job.getTitle());
            System.out.println("- Mô tả: " + job.getDescription());
            System.out.println("- Loại: " + job.getJobType().getValue());
            System.out.println("- Số lượng cần: " + job.getNeededAmount() + " (tự động = 1)");
            System.out.println("- Vị trí: " + job.getLocationAddress());
            System.out.println("- Trạng thái: " + job.getStatus().getValue());
        }
        
        System.out.println("\nQuota còn lại: " + user.getPostQuota());
    }
}
