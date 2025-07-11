package com.example.projectprm392.debug;

import com.example.projectprm392.models.User;
import com.example.projectprm392.services.UserService;

/**
 * Debug class chuyên để test phone number update issue
 */
public class PhoneNumberDebugger {
    
    public static void debugPhoneNumberUpdate() {
        System.out.println("=== DEBUG PHONE NUMBER UPDATE ISSUE ===");
        
        // Tạo user test
        User user = new User("debug@test.com", "pass", "Debug User", "user");
        
        System.out.println("1. USER BAN ĐẦU:");
        printUserInfo(user);
        
        // Test update phone
        String testPhone = "0909123456";
        System.out.println("\n2. ĐANG UPDATE PHONE VỚI: '" + testPhone + "'");
        
        // Validate phone trước
        boolean isValid = UserService.isValidPhoneNumber(testPhone);
        System.out.println("   Phone validation: " + (isValid ? "✅ VALID" : "❌ INVALID"));
        
        if (!isValid) {
            System.out.println("   STOP - Phone không hợp lệ");
            return;
        }
        
        // Update bằng service
        UserService.ProfileUpdateResult result = UserService.updateProfile(
            user, 
            user.getFullName(),  // Keep existing name
            testPhone,           // New phone
            user.getDescription(), // Keep existing description  
            user.getGender()     // Keep existing gender
        );
        
        System.out.println("\n3. KẾT QUẢ UPDATE:");
        System.out.println("   Success: " + result.success);
        System.out.println("   Message: " + result.message);
        
        if (result.success) {
            System.out.println("\n4. USER SAU UPDATE (từ result):");
            printUserInfo(result.user);
            
            System.out.println("\n5. USER GỐC SAU UPDATE:");
            printUserInfo(user);
            
            System.out.println("\n6. SO SÁNH:");
            System.out.println("   result.user == user: " + (result.user == user));
            System.out.println("   Phone result.user: '" + result.user.getPhoneNumber() + "'");
            System.out.println("   Phone user gốc: '" + user.getPhoneNumber() + "'");
            
            boolean phoneEqual = equalStrings(result.user.getPhoneNumber(), user.getPhoneNumber());
            System.out.println("   Phone numbers equal: " + phoneEqual);
            
            if (!phoneEqual) {
                System.out.println("   ❌ VẤN ĐỀ: Phone number không được copy đúng!");
            } else {
                System.out.println("   ✅ Phone number đã được update đúng");
            }
        }
    }
    
    public static void debugStepByStep() {
        System.out.println("\n=== DEBUG STEP BY STEP ===");
        
        User user = new User("step@test.com", "pass", "Step User", "user");
        user.setPhoneNumber("0901111111"); // Set phone ban đầu
        
        System.out.println("STEP 1 - User ban đầu:");
        printUserInfo(user);
        
        System.out.println("\nSTEP 2 - Tạo copy:");
        User copy = user.createCopy();
        printUserInfo(copy);
        System.out.println("Copy == Original: " + (copy == user));
        
        System.out.println("\nSTEP 3 - Update copy:");
        copy.updateProfile(copy.getFullName(), "0909123456", copy.getDescription(), copy.getGender());
        printUserInfo(copy);
        
        System.out.println("\nSTEP 4 - Copy data back:");
        copyUserDataManual(copy, user);
        printUserInfo(user);
        
        System.out.println("\nSTEP 5 - Final check:");
        System.out.println("User phone sau copy: '" + user.getPhoneNumber() + "'");
        System.out.println("Expected: '0909123456'");
        System.out.println("Match: " + "0909123456".equals(user.getPhoneNumber()));
    }
    
    private static void copyUserDataManual(User source, User target) {
        System.out.println("   Copying data...");
        System.out.println("   Source phone: '" + source.getPhoneNumber() + "'");
        System.out.println("   Target phone before: '" + target.getPhoneNumber() + "'");
        
        target.setFullName(source.getFullName());
        target.setPhoneNumber(source.getPhoneNumber());
        target.setDescription(source.getDescription());
        target.setGender(source.getGender());
        
        System.out.println("   Target phone after: '" + target.getPhoneNumber() + "'");
    }
    
    public static void debugValidationOnly() {
        System.out.println("\n=== DEBUG VALIDATION ONLY ===");
        
        String[] testPhones = {
            "0909123456",
            "84909123456",
            "0909 123 456", 
            "+84909123456",
            "abc123",
            "",
            null
        };
        
        for (String phone : testPhones) {
            boolean valid = UserService.isValidPhoneNumber(phone);
            System.out.println(String.format("%-15s -> %s", 
                "'" + phone + "'", 
                valid ? "✅ VALID" : "❌ INVALID"));
        }
    }
    
    public static void debugUIRefreshIssue() {
        System.out.println("\n=== DEBUG UI REFRESH ISSUE ===");
        
        User user = new User("ui@test.com", "pass", "UI User", "user");
        
        System.out.println("SCENARIO: User update phone nhưng UI không hiển thị");
        System.out.println("1. User trước update:");
        printUserInfo(user);
        
        // Update thành công
        UserService.ProfileUpdateResult result = UserService.updateProfile(
            user, user.getFullName(), "0909123456", user.getDescription(), user.getGender()
        );
        
        System.out.println("\n2. Result success: " + result.success);
        System.out.println("   Result message: " + result.message);
        
        if (result.success) {
            System.out.println("\n3. DATA CHECK:");
            System.out.println("   result.user phone: '" + result.user.getPhoneNumber() + "'");
            System.out.println("   original user phone: '" + user.getPhoneNumber() + "'");
            
            System.out.println("\n4. UI REFRESH GUIDELINES:");
            System.out.println("   ✅ ĐÚNG: Sử dụng result.user để update UI");
            System.out.println("   ❌ SAI: Sử dụng user gốc để update UI");
            System.out.println("   ✅ ĐÚNG: Gọi refreshProfileUI(result.user)");
            System.out.println("   ❌ SAI: Gọi refreshProfileUI(user)");
            
            // Simulate correct vs wrong UI update
            System.out.println("\n5. SIMULATION:");
            System.out.println("   updateUI(result.user) -> Phone hiển thị: '" + result.user.getPhoneNumber() + "'");
            System.out.println("   updateUI(user) -> Phone hiển thị: '" + user.getPhoneNumber() + "'");
            
            if (!equalStrings(result.user.getPhoneNumber(), user.getPhoneNumber())) {
                System.out.println("\n   ⚠️ CẢNH BÁO: Hai objects có data khác nhau!");
                System.out.println("   💡 GIẢI PHÁP: Luôn dùng result.user cho UI update");
            }
        }
    }
    
    private static void printUserInfo(User user) {
        if (user == null) {
            System.out.println("   User: null");
            return;
        }
        
        System.out.println("   User ID: " + user.getUserId());
        System.out.println("   Full Name: '" + user.getFullName() + "'");
        System.out.println("   Phone: '" + user.getPhoneNumber() + "'");
        System.out.println("   Description: '" + user.getDescription() + "'");
        System.out.println("   Gender: " + user.getGender());
        System.out.println("   Post Quota: " + user.getPostQuota());
    }
    
    private static boolean equalStrings(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
    
    /**
     * Test chính để run toàn bộ debug
     */
    public static void runAllDebugTests() {
        debugPhoneNumberUpdate();
        debugStepByStep();
        debugValidationOnly();
        debugUIRefreshIssue();
        
        System.out.println("\n=== DEBUG SUMMARY ===");
        System.out.println("Nếu tất cả debug đều OK nhưng UI vẫn không hiển thị phone:");
        System.out.println("1. ✅ Kiểm tra bạn có dùng result.user thay vì user gốc");
        System.out.println("2. ✅ Kiểm tra UI có refresh sau khi update");
        System.out.println("3. ✅ Kiểm tra binding/adapter có update đúng field");
        System.out.println("4. ✅ Kiểm tra format phone number khi hiển thị");
    }
}
