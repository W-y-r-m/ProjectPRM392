package com.example.projectprm392.services;

import com.example.projectprm392.models.User;

/**
 * Service để xử lý User profile và các thao tác liên quan
 */
public class UserService {
    
    /**
     * Cập nhật profile user
     */
    public static ProfileUpdateResult updateProfile(User user, String fullName, String phoneNumber, 
                                                   String description, Boolean gender) {
        if (user == null) {
            return new ProfileUpdateResult(false, "User không hợp lệ", null);
        }
        
        // Validate input
        if (!user.validateProfileUpdate(fullName, phoneNumber)) {
            return new ProfileUpdateResult(false, "Tên không được để trống", null);
        }
        
        // Validate phone number riêng
        if (!isValidPhoneNumber(phoneNumber)) {
            return new ProfileUpdateResult(false, "Số điện thoại không đúng định dạng", null);
        }
        
        // Tạo bản sao để tránh thay đổi object gốc cho đến khi update thành công
        User updatedUser = user.createCopy();
        
        try {
            // Log trước khi update
          
            updatedUser.updateProfile(fullName, phoneNumber, description, gender);
            
           
            copyUserData(updatedUser, user);
            
          
            
            return new ProfileUpdateResult(true, "Cập nhật profile thành công!", user);
            
        } catch (Exception e) {
            return new ProfileUpdateResult(false, "Lỗi khi cập nhật profile: " + e.getMessage(), null);
        }
    }
    
    /**
     * Copy dữ liệu từ source user sang target user với debug logging
     */
    private static void copyUserData(User source, User target) {
        
        
        target.setFullName(source.getFullName());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setDescription(source.getDescription());
        target.setGender(source.getGender());
        // Không copy postQuota - giữ nguyên giá trị cũ
        
    
    }
    
    /**
     * Kiểm tra xem user có thể edit postQuota không
     */
    public static boolean canEditPostQuota(User user) {
        return user != null && user.isPostQuotaEditable();
    }
    
    /**
     * Lấy thông tin hiển thị cho profile
     */
    public static ProfileDisplayInfo getProfileDisplayInfo(User user) {
        if (user == null) {
            return new ProfileDisplayInfo();
        }
        
        ProfileDisplayInfo info = new ProfileDisplayInfo();
        info.fullName = user.getFullName();
        info.email = user.getEmail();
        info.phoneNumber = user.getPhoneNumber();
        info.description = user.getDescription();
        info.gender = user.getGender();
        info.postQuota = user.getPostQuota();
        info.postQuotaStatus = user.getPostQuotaStatus();
        info.canEditQuota = user.isPostQuotaEditable();
        info.needsTopUp = user.needsTopUp();
        info.canPost = user.canPost();
        
        return info;
    }
    
    /**
     * Validate phone number format - Chấp nhận nhiều format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true; // Phone number is optional
        }
        
        // Remove spaces and special characters for validation
        String cleanPhone = phoneNumber.replaceAll("[\\s\\-\\(\\)\\.\\+]", "");
        
        // Check if it's all digits after cleaning
        if (!cleanPhone.matches("^[0-9]+$")) {
            return false;
        }
        
        // Check Vietnamese phone number patterns
        // 0xxxxxxxxx (10 digits starting with 0)
        // 84xxxxxxxxx (11 digits starting with 84)
        // xxxxxxxxx (9 digits without country code)
        if (cleanPhone.matches("^0[0-9]{9}$") ||           // 0909123456
            cleanPhone.matches("^84[0-9]{9}$") ||          // 84909123456
            cleanPhone.matches("^[0-9]{9}$")) {            // 909123456
            return true;
        }
        
        return false;
    }
    
    /**
     * Format phone number for display
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "";
        }
        
        String cleanPhone = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Format Vietnamese phone number: 0xxx xxx xxx
        if (cleanPhone.startsWith("0") && cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 4) + " " + 
                   cleanPhone.substring(4, 7) + " " + 
                   cleanPhone.substring(7);
        }
        
        return phoneNumber; // Return original if can't format
    }
    
    /**
     * Result class cho profile update
     */
    public static class ProfileUpdateResult {
        public boolean success;
        public String message;
        public User user;
        
        public ProfileUpdateResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }
    }
    
    /**
     * Info class cho profile display
     */
    public static class ProfileDisplayInfo {
        public String fullName;
        public String email;
        public String phoneNumber;
        public String description;
        public Boolean gender;
        public Integer postQuota;
        public String postQuotaStatus;
        public boolean canEditQuota;
        public boolean needsTopUp;
        public boolean canPost;
        
        public ProfileDisplayInfo() {}
        
        public String getGenderText() {
            if (gender == null) return "Chưa xác định";
            return gender ? "Nam" : "Nữ";
        }
        
        public String getFormattedQuotaInfo() {
            return String.format("Hạn mức: %d bài (%s)", 
                                postQuota != null ? postQuota : 0, 
                                postQuotaStatus != null ? postQuotaStatus : "");
        }
    }
}
