package com.example.projectprm392.services;

import android.content.Context;
import com.example.projectprm392.models.Job;
import com.example.projectprm392.models.JobSeekingRequest;
import com.example.projectprm392.models.JobPostingRequest;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.LocationUtils;
import java.util.UUID;

/**
 * Service để xử lý việc tạo Job với quota checking
 */
public class JobService {
    
    private Context context;
    
    public JobService(Context context) {
        this.context = context;
    }
    
    /**
     * Tạo job seeking (đăng tin tìm việc)
     * Chỉ cần title và description
     * Vị trí sẽ được cập nhật từ Google Maps hoặc vị trí hiện tại
     */
    public Job createJobSeeking(JobSeekingRequest request, UUID userId, boolean useCurrentLocation) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Thông tin job seeking không hợp lệ");
        }
        
        // Nếu sử dụng vị trí hiện tại
        if (useCurrentLocation) {
            LocationUtils.LocationData currentLocation = LocationUtils.getCurrentLocation(context);
            if (currentLocation != null) {
                request.setLocationLatitude(currentLocation.latitude);
                request.setLocationLongitude(currentLocation.longitude);
                request.setLocationAddress(currentLocation.address);
            }
        }
        
        Job job = request.toJob(userId);
        
        // Log thông tin job được tạo
        System.out.println("Đã tạo job seeking: " + job.getTitle());
        System.out.println("Vị trí: " + job.getLocationAddress());
        
        return job;
    }
    
    /**
     * Tạo job posting (đăng tin tuyển dụng)
     * Dựa trên dữ liệu từ bảng Job với đầy đủ thông tin
     */
    public Job createJobPosting(JobPostingRequest request, UUID userId) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("Thông tin job posting không hợp lệ");
        }
        
        // Nếu sử dụng vị trí hiện tại
        if (request.isUseCurrentLocation()) {
            LocationUtils.LocationData currentLocation = LocationUtils.getCurrentLocation(context);
            if (currentLocation != null) {
                request.setLocationLatitude(currentLocation.latitude);
                request.setLocationLongitude(currentLocation.longitude);
                request.setLocationAddress(currentLocation.address);
            }
        }
        
        Job job = request.toJob(userId);
        
        // Log thông tin job được tạo
        System.out.println("Đã tạo job posting: " + job.getTitle());
        System.out.println("Loại công việc: " + job.getCategory().getValue());
        System.out.println("Cấp độ: " + job.getLevel().getValue());
        System.out.println("Lương: " + job.getFormattedSalary());
        System.out.println("Vị trí: " + job.getLocationAddress());
        
        return job;
    }
    
    /**
     * Cập nhật vị trí cho job từ Google Maps
     */
    public void updateJobLocation(Job job, double latitude, double longitude) {
        job.setLocationLatitude(latitude);
        job.setLocationLongitude(longitude);
        
        String address = LocationUtils.getAddressFromCoordinates(context, latitude, longitude);
        job.setLocationAddress(address);
        
        System.out.println("Đã cập nhật vị trí cho job: " + job.getTitle());
        System.out.println("Vị trí mới: " + address);
    }
    
    /**
     * Tìm kiếm job theo khoảng cách
     */
    public double calculateDistanceToJob(Job job, double userLatitude, double userLongitude) {
        if (job.getLocationLatitude() == null || job.getLocationLongitude() == null) {
            return -1; // Không có thông tin vị trí
        }
        
        return LocationUtils.calculateDistance(
            userLatitude, userLongitude,
            job.getLocationLatitude(), job.getLocationLongitude()
        );
    }
    
    /**
     * Validate job data
     */
    public boolean validateJob(Job job) {
        if (job == null) return false;
        
        // Validate chung
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) return false;
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) return false;
        if (job.getUserId() == null) return false;
        
        // Validate theo loại job
        if (job.isJobSeeking()) {
            // Job seeking không cần validate neededAmount vì luôn mặc định = 1
            return true;
        } else if (job.isJobPosting()) {
            return job.getSalary() != null && job.getSalary().doubleValue() > 0 &&
                   job.getSalaryUnit() != null &&
                   job.getCategory() != null &&
                   job.getLevel() != null;
        }
        
        return false;
    }
    
    /**
     * Tạo job seeking (đăng tin tìm việc) với quota checking
     * Chỉ cần title và description
     * Vị trí sẽ được cập nhật từ Google Maps hoặc vị trí hiện tại
     */
    public JobCreationResult createJobSeekingWithQuota(JobSeekingRequest request, User user, boolean useCurrentLocation) {
        // Kiểm tra quota trước
        if (QuotaService.needsTopUp(user)) {
            return new JobCreationResult(false, QuotaService.getQuotaMessage(user), null);
        }
        
        if (!QuotaService.canPost(user)) {
            return new JobCreationResult(false, "Không thể đăng tin. " + QuotaService.getQuotaMessage(user), null);
        }
        
        if (!request.isValid()) {
            return new JobCreationResult(false, "Thông tin job seeking không hợp lệ", null);
        }
        
        // Xử lý quota
        QuotaService.PostResult postResult = QuotaService.processPost(user);
        if (!postResult.success) {
            return new JobCreationResult(false, postResult.message, null);
        }
        
        // Tạo job
        try {
            Job job = createJobSeeking(request, user.getUserId(), useCurrentLocation);
            return new JobCreationResult(true, postResult.message, job);
        } catch (Exception e) {
            // Hoàn lại quota nếu tạo job thất bại
            user.addPostQuota(1);
            return new JobCreationResult(false, "Lỗi khi tạo job: " + e.getMessage(), null);
        }
    }

    /**
     * Tạo job posting (đăng tin tuyển dụng) với quota checking
     */
    public JobCreationResult createJobPostingWithQuota(JobPostingRequest request, User user) {
        // Kiểm tra quota trước
        if (QuotaService.needsTopUp(user)) {
            return new JobCreationResult(false, QuotaService.getQuotaMessage(user), null);
        }
        
        if (!QuotaService.canPost(user)) {
            return new JobCreationResult(false, "Không thể đăng tin. " + QuotaService.getQuotaMessage(user), null);
        }
        
        if (!request.isValid()) {
            return new JobCreationResult(false, "Thông tin job posting không hợp lệ", null);
        }
        
        // Xử lý quota
        QuotaService.PostResult postResult = QuotaService.processPost(user);
        if (!postResult.success) {
            return new JobCreationResult(false, postResult.message, null);
        }
        
        // Tạo job
        try {
            Job job = createJobPosting(request, user.getUserId());
            return new JobCreationResult(true, postResult.message, job);
        } catch (Exception e) {
            // Hoàn lại quota nếu tạo job thất bại
            user.addPostQuota(1);
            return new JobCreationResult(false, "Lỗi khi tạo job: " + e.getMessage(), null);
        }
    }

    // Result class for job creation
    public static class JobCreationResult {
        public boolean success;
        public String message;
        public Job job;
        
        public JobCreationResult(boolean success, String message, Job job) {
            this.success = success;
            this.message = message;
            this.job = job;
        }
    }
}
