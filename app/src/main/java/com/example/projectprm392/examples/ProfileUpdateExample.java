package com.example.projectprm392.examples;

import com.example.projectprm392.models.User;
import com.example.projectprm392.services.UserService;

/**
 * Ví dụ về cách xử lý Profile Update và Quota Management
 */
public class ProfileUpdateExample {
    
    public static void demonstrateProfileUpdate() {
        System.out.println("=== VÍ DỤ PROFILE UPDATE ===");
        
        // Tạo user mẫu
        User user = new User("test@example.com", "password", "Nguyễn Văn A", "user");
        user.setPhoneNumber("0909123456");
        user.setDescription("Mô tả cũ");
        user.setGender(true); // Nam
        user.setPostQuota(15); // Quota bình thường
        
        System.out.println("THÔNG TIN TRƯỚC KHI CẬP NHẬT:");
        displayUserInfo(user);
        
        // Thử cập nhật profile
        String newFullName = "Nguyễn Văn B";
        String newPhoneNumber = "0909987654";
        String newDescription = "Mô tả mới - đã cập nhật";
        Boolean newGender = false; // Nữ
        
        UserService.ProfileUpdateResult result = UserService.updateProfile(
            user, newFullName, newPhoneNumber, newDescription, newGender
        );
        
        System.out.println("\nKẾT QUẢ CẬP NHẬT:");
        System.out.println("Trạng thái: " + (result.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Thông báo: " + result.message);
        
        if (result.success) {
            System.out.println("\nTHÔNG TIN SAU KHI CẬP NHẬT:");
            displayUserInfo(result.user);
        }
    }
    
    public static void demonstrateQuotaEditRestriction() {
        System.out.println("\n=== VÍ DỤ HẠN CHẾ EDIT QUOTA ===");
        
        User user1 = new User("user1@test.com", "pass", "User 1", "user");
        user1.setPostQuota(10);
        
        User user2 = new User("user2@test.com", "pass", "User 2", "admin");
        user2.setPostQuota(25);
        
        testQuotaEdit(user1);
        testQuotaEdit(user2);
    }
    
    private static void testQuotaEdit(User user) {
        System.out.println("\n--- User: " + user.getFullName() + " ---");
        System.out.println("Post Quota: " + user.getPostQuota());
        System.out.println("Có thể edit quota: " + (UserService.canEditPostQuota(user) ? "CÓ" : "KHÔNG"));
        System.out.println("Trạng thái quota: " + user.getPostQuotaStatus());
        System.out.println("Cần nạp tiền: " + (user.needsTopUp() ? "CÓ" : "KHÔNG"));
        System.out.println("Có thể đăng tin: " + (user.canPost() ? "CÓ" : "KHÔNG"));
    }
    
    public static void demonstrateProfileDisplay() {
        System.out.println("\n=== VÍ DỤ HIỂN THỊ PROFILE ===");
        
        User user = new User("display@test.com", "pass", "Trần Thị C", "user");
        user.setPhoneNumber("0909111222");
        user.setDescription("Tôi là một developer Android");
        user.setGender(false); // Nữ
        user.setPostQuota(18);
        
        UserService.ProfileDisplayInfo displayInfo = UserService.getProfileDisplayInfo(user);
        
        System.out.println("THÔNG TIN HIỂN THỊ:");
        System.out.println("Họ tên: " + displayInfo.fullName);
        System.out.println("Email: " + displayInfo.email);
        System.out.println("Số điện thoại: " + UserService.formatPhoneNumber(displayInfo.phoneNumber));
        System.out.println("Giới tính: " + displayInfo.getGenderText());
        System.out.println("Mô tả: " + displayInfo.description);
        System.out.println("Hạn mức đăng tin: " + displayInfo.getFormattedQuotaInfo());
        System.out.println("Có thể edit quota: " + (displayInfo.canEditQuota ? "CÓ" : "KHÔNG"));
        System.out.println("Cần nạp tiền: " + (displayInfo.needsTopUp ? "CÓ" : "KHÔNG"));
    }
    
    public static void demonstrateValidation() {
        System.out.println("\n=== VÍ DỤ VALIDATION ===");
        
        User user = new User("validation@test.com", "pass", "Test User", "user");
        
        // Test các trường hợp validation
        String[] testFullNames = {"", "   ", "Nguyễn Văn A", null};
        String[] testPhoneNumbers = {"0909123456", "invalid", "84909123456", "+84909123456", ""};
        
        System.out.println("Test Full Name Validation:");
        for (String fullName : testFullNames) {
            boolean valid = user.validateProfileUpdate(fullName, "0909123456");
            System.out.println("'" + fullName + "' -> " + (valid ? "✅ Hợp lệ" : "❌ Không hợp lệ"));
        }
        
        System.out.println("\nTest Phone Number Validation:");
        for (String phone : testPhoneNumbers) {
            boolean valid = UserService.isValidPhoneNumber(phone);
            String formatted = UserService.formatPhoneNumber(phone);
            System.out.println("'" + phone + "' -> " + (valid ? "✅ Hợp lệ" : "❌ Không hợp lệ") + 
                             " | Formatted: '" + formatted + "'");
        }
    }
    
    public static void demonstrateFailedUpdate() {
        System.out.println("\n=== VÍ DỤ CẬP NHẬT THẤT BẠI ===");
        
        User user = new User("fail@test.com", "pass", "User Test", "user");
        
        // Test update với dữ liệu không hợp lệ
        UserService.ProfileUpdateResult result1 = UserService.updateProfile(
            user, "", "0909123456", "Description", true
        );
        
        System.out.println("Update với tên rỗng:");
        System.out.println("Kết quả: " + (result1.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Thông báo: " + result1.message);
        
        // Test update với phone không hợp lệ
        UserService.ProfileUpdateResult result2 = UserService.updateProfile(
            user, "Valid Name", "invalid_phone", "Description", true
        );
        
        System.out.println("\nUpdate với phone không hợp lệ:");
        System.out.println("Kết quả: " + (result2.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Thông báo: " + result2.message);
        
        // Test update với null user
        UserService.ProfileUpdateResult result3 = UserService.updateProfile(
            null, "Valid Name", "0909123456", "Description", true
        );
        
        System.out.println("\nUpdate với null user:");
        System.out.println("Kết quả: " + (result3.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Thông báo: " + result3.message);
    }
    
    private static void displayUserInfo(User user) {
        if (user == null) {
            System.out.println("User is null");
            return;
        }
        
        System.out.println("- Họ tên: " + user.getFullName());
        System.out.println("- Email: " + user.getEmail());
        System.out.println("- Số điện thoại: " + UserService.formatPhoneNumber(user.getPhoneNumber()));
        System.out.println("- Mô tả: " + user.getDescription());
        System.out.println("- Giới tính: " + (user.getGender() != null ? (user.getGender() ? "Nam" : "Nữ") : "Chưa xác định"));
        System.out.println("- Post Quota: " + user.getPostQuota() + " (" + user.getPostQuotaStatus() + ")");
        System.out.println("- Có thể edit quota: " + (user.isPostQuotaEditable() ? "CÓ" : "KHÔNG"));
    }
}
