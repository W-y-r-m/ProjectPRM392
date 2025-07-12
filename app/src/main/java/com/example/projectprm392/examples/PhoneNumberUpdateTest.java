package com.example.projectprm392.examples;

import com.example.projectprm392.models.User;
import com.example.projectprm392.services.UserService;

/**
 * Test specific cho Phone Number Update Issue
 */
public class PhoneNumberUpdateTest {
    
    public static void testPhoneNumberUpdate() {
        System.out.println("=== TEST PHONE NUMBER UPDATE ===");
        
        // Tạo user không có phone number
        User user = new User("test@example.com", "password", "Test User", "user");
        System.out.println("User ban đầu:");
        System.out.println("- Phone: '" + user.getPhoneNumber() + "'");
        
        // Test các format phone number khác nhau
        String[] testPhones = {
            "0909123456",      // Standard format
            "84909123456",     // With country code
            "0909 123 456",    // With spaces
            "0909-123-456",    // With dashes
            "0909.123.456",    // With dots
            "(0909) 123 456",  // With parentheses
            "+84909123456",    // With plus
            "909123456",       // Without leading 0
            "",                // Empty
            null               // Null
        };
        
        for (String phone : testPhones) {
            testSinglePhoneUpdate(user, phone);
        }
    }
    
    private static void testSinglePhoneUpdate(User user, String newPhone) {
        System.out.println("\n--- Test phone: '" + newPhone + "' ---");
        
        // Test validation trước
        boolean isValid = UserService.isValidPhoneNumber(newPhone);
        System.out.println("Validation: " + (isValid ? "✅ Hợp lệ" : "❌ Không hợp lệ"));
        
        if (!isValid) {
            System.out.println("Bỏ qua test update vì validation fail");
            return;
        }
        
        // Reset user phone để test
        user.setPhoneNumber(null);
        
        // Test update
        UserService.ProfileUpdateResult result = UserService.updateProfile(
            user, user.getFullName(), newPhone, user.getDescription(), user.getGender()
        );
        
        System.out.println("Update result: " + (result.success ? "✅ THÀNH CÔNG" : "❌ THẤT BẠI"));
        System.out.println("Message: " + result.message);
        
        if (result.success) {
            System.out.println("Phone sau update: '" + result.user.getPhoneNumber() + "'");
            String formatted = UserService.formatPhoneNumber(result.user.getPhoneNumber());
            System.out.println("Phone formatted: '" + formatted + "'");
        }
    }
    
    public static void testDetailedUpdate() {
        System.out.println("\n=== TEST CHI TIẾT CẬP NHẬT ===");
        
        User user = new User("detail@test.com", "pass", "Detail User", "user");
        user.setPhoneNumber("0901111111"); // Set phone ban đầu
        user.setDescription("Mô tả ban đầu");
        user.setGender(true);
        
        System.out.println("TRƯỚC UPDATE:");
        displayUserDetail(user);
        
        // Update với phone mới
        String newPhone = "0909123456";
        String newDescription = "Mô tả mới đã cập nhật";
        Boolean newGender = false;
        
        System.out.println("\nĐANG UPDATE VỚI:");
        System.out.println("- New Phone: '" + newPhone + "'");
        System.out.println("- New Description: '" + newDescription + "'");
        System.out.println("- New Gender: " + newGender);
        
        UserService.ProfileUpdateResult result = UserService.updateProfile(
            user, user.getFullName(), newPhone, newDescription, newGender
        );
        
        System.out.println("\nKẾT QUẢ UPDATE:");
        System.out.println("Success: " + result.success);
        System.out.println("Message: " + result.message);
        
        System.out.println("\nSAU UPDATE:");
        displayUserDetail(result.user);
        
        // Kiểm tra xem có thay đổi không
        System.out.println("\nSO SÁNH:");
        System.out.println("Phone thay đổi: " + (!equalStrings(user.getPhoneNumber(), newPhone) ? "❌ KHÔNG" : "✅ CÓ"));
        System.out.println("Description thay đổi: " + (!equalStrings(user.getDescription(), newDescription) ? "❌ KHÔNG" : "✅ CÓ"));
        System.out.println("Gender thay đổi: " + (!newGender.equals(user.getGender()) ? "❌ KHÔNG" : "✅ CÓ"));
    }
    
    private static void displayUserDetail(User user) {
        if (user == null) {
            System.out.println("User is null");
            return;
        }
        
        System.out.println("- FullName: '" + user.getFullName() + "'");
        System.out.println("- Phone: '" + user.getPhoneNumber() + "'");
        System.out.println("- Description: '" + user.getDescription() + "'");
        System.out.println("- Gender: " + user.getGender());
        System.out.println("- PostQuota: " + user.getPostQuota());
    }
    
    private static boolean equalStrings(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
    
    public static void testValidationOnly() {
        System.out.println("\n=== TEST VALIDATION CHI TIẾT ===");
        
        String[] phones = {
            "0909123456",
            "84909123456", 
            "+84909123456",
            "0909 123 456",
            "0909-123-456",
            "abc123456",
            "123",
            "090912345678901234",
            "",
            null
        };
        
        for (String phone : phones) {
            boolean valid = UserService.isValidPhoneNumber(phone);
            System.out.println(String.format("%-20s -> %s", 
                "'" + phone + "'", 
                valid ? "✅ Valid" : "❌ Invalid"));
        }
    }
}
