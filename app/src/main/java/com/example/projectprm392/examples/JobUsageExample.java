package com.example.projectprm392.examples;

import android.content.Context;
import com.example.projectprm392.models.Job;
import com.example.projectprm392.models.JobSeekingRequest;
import com.example.projectprm392.models.JobPostingRequest;
import com.example.projectprm392.services.JobService;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

/**
 * Ví dụ về cách sử dụng Job system
 */
public class JobUsageExample {
    
    public static void demonstrateJobSeeking(Context context) {
        System.out.println("=== VÍ DỤ ĐĂNG TIN TÌM VIỆC ===");
        
        JobService jobService = new JobService(context);
        UUID userId = UUID.randomUUID();
        
        // Tạo job seeking request - chỉ cần 2 thông tin cơ bản
        JobSeekingRequest seekingRequest = new JobSeekingRequest();
        seekingRequest.setTitle("Tìm việc lập trình Android");
        seekingRequest.setDescription("Tôi đang tìm việc làm lập trình Android part-time, có kinh nghiệm 2 năm với Java và Kotlin");
        
        try {
            // Tạo job seeking với vị trí hiện tại
            Job jobSeeking = jobService.createJobSeeking(seekingRequest, userId, true);
            
            System.out.println("Đã tạo thành công job seeking:");
            System.out.println("- Tiêu đề: " + jobSeeking.getTitle());
            System.out.println("- Mô tả: " + jobSeeking.getDescription());
            System.out.println("- Loại: " + jobSeeking.getJobType().getValue());
            System.out.println("- Vị trí: " + jobSeeking.getLocationAddress());
            
        } catch (Exception e) {
            System.out.println("Lỗi khi tạo job seeking: " + e.getMessage());
        }
    }
    
    public static void demonstrateJobPosting(Context context) {
        System.out.println("\n=== VÍ DỤ ĐĂNG TIN TUYỂN DỤNG ===");
        
        JobService jobService = new JobService(context);
        UUID userId = UUID.randomUUID();
        
        // Tạo job posting request - đầy đủ thông tin
        JobPostingRequest postingRequest = new JobPostingRequest();
        postingRequest.setTitle("Tuyển Android Developer");
        postingRequest.setDescription("Công ty ABC tuyển Android Developer, làm việc tại văn phòng, dự án ứng dụng mobile");
        postingRequest.setSalary(new BigDecimal("20000000")); // 20 triệu VND
        postingRequest.setSalaryUnit(Job.SalaryUnit.DAY);
        postingRequest.setCategory(Job.JobCategory.TECHNOLOGY);
        postingRequest.setLevel(Job.JobLevel.MID_LEVEL);
        postingRequest.setNeededAmount(2); // Cần tuyển 2 người
        
        // Thời gian làm việc
        postingRequest.setStartTime(Time.valueOf("08:00:00"));
        postingRequest.setEndTime(Time.valueOf("17:00:00"));
        postingRequest.setWorkingDate(new Date(System.currentTimeMillis()));
        
        // Vị trí cụ thể (có thể chọn trên Google Maps)
        postingRequest.setLocationLatitude(10.7769);
        postingRequest.setLocationLongitude(106.7009);
        postingRequest.setLocationAddress("Quận 1, TP.HCM");
        
        try {
            Job jobPosting = jobService.createJobPosting(postingRequest, userId);
            
            System.out.println("Đã tạo thành công job posting:");
            System.out.println("- Tiêu đề: " + jobPosting.getTitle());
            System.out.println("- Mô tả: " + jobPosting.getDescription());
            System.out.println("- Lương: " + jobPosting.getFormattedSalary());
            System.out.println("- Loại công việc: " + jobPosting.getCategory().getValue());
            System.out.println("- Cấp độ: " + jobPosting.getLevel().getValue());
            System.out.println("- Số lượng cần tuyển: " + jobPosting.getNeededAmount());
            System.out.println("- Thời gian làm việc: " + jobPosting.getFormattedWorkingTime());
            System.out.println("- Vị trí: " + jobPosting.getLocationAddress());
            
        } catch (Exception e) {
            System.out.println("Lỗi khi tạo job posting: " + e.getMessage());
        }
    }
    
    public static void demonstrateLocationUpdate(Context context) {
        System.out.println("\n=== VÍ DỤ CẬP NHẬT VỊ TRÍ ===");
        
        JobService jobService = new JobService(context);
        UUID userId = UUID.randomUUID();
        
        // Tạo một job đơn giản
        JobSeekingRequest request = new JobSeekingRequest("Tìm việc thiết kế", "Tìm việc thiết kế UI/UX");
        Job job = jobService.createJobSeeking(request, userId, false);
        
        System.out.println("Job trước khi cập nhật vị trí:");
        System.out.println("- Vị trí: " + job.getLocationAddress());
        
        // Cập nhật vị trí mới (từ Google Maps)
        double newLat = 10.8231;
        double newLng = 106.6297;
        jobService.updateJobLocation(job, newLat, newLng);
        
        System.out.println("Job sau khi cập nhật vị trí:");
        System.out.println("- Vị trí: " + job.getLocationAddress());
        System.out.println("- Tọa độ: " + job.getLocationLatitude() + ", " + job.getLocationLongitude());
    }
    
    public static void demonstrateAllJobCategories() {
        System.out.println("\n=== DANH SÁCH CÁC LOẠI CÔNG VIỆC ===");
        
        for (Job.JobCategory category : Job.JobCategory.values()) {
            System.out.println("- " + category.getValue());
        }
    }
    
    public static void demonstrateAllJobLevels() {
        System.out.println("\n=== DANH SÁCH CÁC CẤP ĐỘ CÔNG VIỆC ===");
        
        for (Job.JobLevel level : Job.JobLevel.values()) {
            System.out.println("- " + level.getValue());
        }
    }
}
