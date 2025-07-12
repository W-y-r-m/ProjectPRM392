package com.example.projectprm392.ui;

import com.example.projectprm392.models.User;
import com.example.projectprm392.services.UserService;

/**
 * UI Pattern Guide cho Profile Update
 * Giải quyết vấn đề "update thành công nhưng UI không hiển thị"
 */
public class ProfileUpdateUIPattern {
    
    /**
     * ❌ PATTERN SAI - Gây ra vấn đề UI không cập nhật
     */
    public static class WrongPattern {
        
        public void updateProfileWrong(User currentUser, String newPhone) {
            // SAI: Update và dùng user gốc
            UserService.ProfileUpdateResult result = UserService.updateProfile(
                currentUser, currentUser.getFullName(), newPhone, 
                currentUser.getDescription(), currentUser.getGender()
            );
            
            if (result.success) {
                showMessage("Cập nhật thành công");
                
                // ❌ SAI: Dùng currentUser để refresh UI
                refreshProfileUI(currentUser); // Phone có thể không hiển thị!
            }
        }
        
        public void refreshProfileUI(User user) {
            // UI bind với user object cũ
            // phoneTextField.setText(user.getPhoneNumber()); // Có thể null hoặc cũ
        }
        
        private void showMessage(String message) {
            System.out.println(message);
        }
    }
    
    /**
     * ✅ PATTERN ĐÚNG - Đảm bảo UI cập nhật đúng
     */
    public static class CorrectPattern {
        
        public void updateProfileCorrect(User currentUser, String newPhone) {
            UserService.ProfileUpdateResult result = UserService.updateProfile(
                currentUser, currentUser.getFullName(), newPhone, 
                currentUser.getDescription(), currentUser.getGender()
            );
            
            if (result.success) {
                showMessage("✅ " + result.message);
                
                // ✅ ĐÚNG: Dùng result.user để refresh UI
                refreshProfileUI(result.user);
                
                // ✅ ĐÚNG: Update user trong session/memory
                updateCurrentUserInSession(result.user);
                
            } else {
                showError("❌ " + result.message);
            }
        }
        
        public void refreshProfileUI(User user) {
            // Sử dụng user object đã được update
            updateFullNameField(user.getFullName());
            updatePhoneField(user.getPhoneNumber());
            updateDescriptionField(user.getDescription());
            updateGenderField(user.getGender());
            updateQuotaDisplay(user.getPostQuota(), user.getPostQuotaStatus());
        }
        
        private void updateFullNameField(String fullName) {
            // fullNameTextField.setText(fullName);
            System.out.println("UI: FullName = " + fullName);
        }
        
        private void updatePhoneField(String phoneNumber) {
            // Format phone cho hiển thị
            String formattedPhone = UserService.formatPhoneNumber(phoneNumber);
            // phoneTextField.setText(formattedPhone);
            System.out.println("UI: Phone = " + formattedPhone);
        }
        
        private void updateDescriptionField(String description) {
            // descriptionTextField.setText(description);
            System.out.println("UI: Description = " + description);
        }
        
        private void updateGenderField(Boolean gender) {
            String genderText = gender != null ? (gender ? "Nam" : "Nữ") : "Chưa xác định";
            // genderSpinner.setSelection(...);
            System.out.println("UI: Gender = " + genderText);
        }
        
        private void updateQuotaDisplay(Integer quota, String status) {
            // quotaTextView.setText(quota + " (" + status + ")");
            System.out.println("UI: Quota = " + quota + " (" + status + ")");
        }
        
        private void updateCurrentUserInSession(User updatedUser) {
            // Cập nhật user trong session manager hoặc shared preferences
            // SessionManager.setCurrentUser(updatedUser);
            System.out.println("Session: User updated");
        }
        
        private void showMessage(String message) {
            System.out.println(message);
        }
        
        private void showError(String message) {
            System.out.println(message);
        }
    }
    
    /**
     * Test để so sánh hai patterns
     */
    public static void testPatterns() {
        System.out.println("=== TEST UI PATTERNS ===");
        
        // Setup test user
        User testUser = new User("test@ui.com", "pass", "Test User", "user");
        testUser.setPhoneNumber("0901111111");
        
        System.out.println("User ban đầu:");
        System.out.println("Phone: " + testUser.getPhoneNumber());
        
        String newPhone = "0909123456";
        System.out.println("\nUpdate phone thành: " + newPhone);
        
        // Test wrong pattern
        System.out.println("\n--- WRONG PATTERN ---");
        WrongPattern wrongPattern = new WrongPattern();
        wrongPattern.updateProfileWrong(testUser, newPhone);
        
        // Reset user
        testUser.setPhoneNumber("0901111111");
        
        // Test correct pattern  
        System.out.println("\n--- CORRECT PATTERN ---");
        CorrectPattern correctPattern = new CorrectPattern();
        correctPattern.updateProfileCorrect(testUser, newPhone);
    }
    
    /**
     * Checklist cho developers
     */
    public static void printChecklist() {
        System.out.println("\n=== CHECKLIST PROFILE UPDATE ===");
        System.out.println("✅ 1. Sử dụng UserService.updateProfile() thay vì set trực tiếp");
        System.out.println("✅ 2. Kiểm tra result.success trước khi update UI");
        System.out.println("✅ 3. Dùng result.user cho UI refresh, KHÔNG dùng user gốc");
        System.out.println("✅ 4. Update user trong session/memory với result.user");
        System.out.println("✅ 5. Format phone number khi hiển thị UI");
        System.out.println("✅ 6. Handle null values trong UI binding");
        System.out.println("✅ 7. Show error message nếu result.success = false");
        System.out.println("✅ 8. Test với debug logging để verify data flow");
    }
    
    /**
     * Common mistakes developers làm
     */
    public static void printCommonMistakes() {
        System.out.println("\n=== COMMON MISTAKES ===");
        System.out.println("❌ 1. Dùng user gốc thay vì result.user cho UI update");
        System.out.println("❌ 2. Không update user trong session sau khi profile update");
        System.out.println("❌ 3. Không format phone number khi hiển thị");
        System.out.println("❌ 4. Không handle null phone number");
        System.out.println("❌ 5. Bind UI trước khi kiểm tra result.success");
        System.out.println("❌ 6. Không refresh adapter nếu dùng RecyclerView");
        System.out.println("❌ 7. Cache user object cũ trong fragment/activity");
    }
}
