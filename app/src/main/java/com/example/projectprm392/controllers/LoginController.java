package com.example.projectprm392.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.projectprm392.api.DatabaseApiService;
import com.example.projectprm392.viewmodels.LoginRequest;
import com.example.projectprm392.viewmodels.LoginResponse;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.utils.ValidationUtils;
import com.example.projectprm392.utils.EmailService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController {
    private static final String TAG = "LoginController";
    
    public interface LoginCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }
    
    public interface RegisterCallback {
        void onSuccess(LoginResponse response);
        void onError(String error);
    }
    
    public interface VerificationCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface EmailExistsCallback {
        void onEmailExists(String fullName);
        void onEmailNotExists();
        void onError(String error);
    }
    
    public interface UpdatePasswordCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface UserInfoCallback {
        void onSuccess(String fullName);
        void onError(String error);
    }
    
    private DatabaseApiService databaseApiService;
    private SessionManager sessionManager;
    private Context context;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public LoginController(Context context) {
        this.context = context;
        this.databaseApiService = new DatabaseApiService(context);
        this.sessionManager = new SessionManager(context);
        this.executorService = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Ensure database is initialized with sample data
        ensureDatabaseInitialized();
    }
    
    private void ensureDatabaseInitialized() {
        executorService.execute(() -> {
            try {
                // Chỉ initialize một lần khi cần thiết
                if (!isDatabaseInitialized()) {
                    databaseApiService.initializeSampleData();
                    Log.d(TAG, "Database initialization completed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database: " + e.getMessage());
            }
        });
    }
    
    private boolean isDatabaseInitialized() {
        // Kiểm tra xem admin user đã tồn tại chưa
        try {
            return databaseApiService.getUserByEmail("admin@gmail.com") != null;
        } catch (Exception e) {
            Log.e(TAG, "Error checking database initialization: " + e.getMessage());
            return false;
        }
    }
    
    public void forceInitializeDatabase() {
        ensureDatabaseInitialized();
    }
    
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }
      public void login(String email, String password, LoginCallback callback) {
        // Validate input
        String emailError = ValidationUtils.getEmailError(email);
        if (emailError != null) {
            callback.onError(emailError);
            return;
        }

        String passwordError = ValidationUtils.getPasswordError(password);
        if (passwordError != null) {
            callback.onError(passwordError);
            return;
        }

        // Sử dụng plain text password
        LoginRequest request = new LoginRequest(email, password);
        
        // Sử dụng SQLite Database
        executorService.execute(() -> {
            try {
                LoginResponse response = databaseApiService.login(request);
                
                mainHandler.post(() -> {
                    if (response.isSuccess()) {
                        // Login successful, save session
                        User user = response.getUser();
                        if (user != null) {
                            sessionManager.saveUserSession(
                                user.getUserId().toString(),
                                user.getEmail(),
                                user.getFullName(),
                                user.getRole(),
                                response.getToken()
                            );
                        }
                        callback.onSuccess(response);
                    } else {
                        callback.onError(response.getMessage());
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        });
    }
      public void register(User user, RegisterCallback callback) {
        // Validate user data
        String emailError = ValidationUtils.getEmailError(user.getEmail());
        if (emailError != null) {
            callback.onError(emailError);
            return;
        }

        String passwordError = ValidationUtils.getPasswordError(user.getPassword());
        if (passwordError != null) {
            callback.onError(passwordError);
            return;
        }

        String nameError = ValidationUtils.getFullNameError(user.getFullName());
        if (nameError != null) {
            callback.onError(nameError);
            return;
        }

        // Sử dụng SQLite Database
        executorService.execute(() -> {
            try {
                // Không hash password - sử dụng plain text
                LoginResponse response = databaseApiService.register(user);
                
                mainHandler.post(() -> {
                    if (response.isSuccess()) {
                        callback.onSuccess(response);
                    } else {
                        callback.onError(response.getMessage());
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi: " + e.getMessage()));
            }
        });
    }
    
    public void logout() {
        sessionManager.logout();
        // Cleanup database connection nếu cần
        if (databaseApiService != null) {
            databaseApiService.close();
        }
    }
    
    public User getCurrentUser() {
        if (!sessionManager.isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setUserId(java.util.UUID.fromString(sessionManager.getUserId()));
        user.setEmail(sessionManager.getEmail());
        user.setFullName(sessionManager.getFullName());
        user.setRole(sessionManager.getRole());
        
        return user;
    }
    
    public void sendVerificationEmail(String email, String fullName, EmailService.EmailCallback callback) {
        executorService.execute(() -> {
            try {
                // Use EmailJS with context for real email sending
                EmailService.sendVerificationEmailWithContext(context, email, fullName, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess(String verificationCode) {
                        // Lưu verification code vào database với thời gian hết hạn
                        Calendar calendar = Calendar.getInstance();
                        calendar.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
                        Date expiresAt = calendar.getTime();
                        
                        databaseApiService.updateVerificationCode(email, verificationCode, expiresAt);
                        callback.onSuccess(verificationCode);
                    }
                    
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error sending verification email", e);
                callback.onError("Lỗi gửi email xác thực");
            }
        });
    }
    
    public void verifyEmail(String email, String code, VerificationCallback callback) {
        executorService.execute(() -> {
            try {
                Date currentTime = new Date();
                boolean isVerified = databaseApiService.verifyUser(email, code, currentTime);
                
                mainHandler.post(() -> {
                    if (isVerified) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Mã xác thực không đúng hoặc đã hết hạn");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error verifying email", e);
                mainHandler.post(() -> callback.onError("Lỗi xác thực email"));
            }
        });
    }
    
    public void updateVerificationCode(String email, String code) {
        executorService.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 15);
                Date expiresAt = calendar.getTime();
                
                databaseApiService.updateVerificationCode(email, code, expiresAt);
            } catch (Exception e) {
                Log.e(TAG, "Error updating verification code", e);
            }
        });
    }
    
    /**
     * Kiểm tra email có tồn tại trong hệ thống không
     */
    public void checkEmailExists(String email, EmailExistsCallback callback) {
        executorService.execute(() -> {
            try {
                User user = databaseApiService.getUserByEmail(email);
                
                mainHandler.post(() -> {
                    if (user != null) {
                        callback.onEmailExists(user.getFullName());
                    } else {
                        callback.onEmailNotExists();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error checking email exists", e);
                mainHandler.post(() -> callback.onError("Lỗi kiểm tra email"));
            }
        });
    }
    
    /**
     * Lưu mã reset password vào database
     */
    public void savePasswordResetCode(String email, String resetCode) {
        executorService.execute(() -> {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
                Date expiresAt = calendar.getTime();
                
                databaseApiService.updateVerificationCode(email, resetCode, expiresAt);
            } catch (Exception e) {
                Log.e(TAG, "Error saving password reset code", e);
            }
        });
    }
    
    /**
     * Xác thực mã reset password
     */
    public void verifyPasswordResetCode(String email, String resetCode, VerificationCallback callback) {
        executorService.execute(() -> {
            try {
                Date currentTime = new Date();
                boolean isValid = databaseApiService.verifyUser(email, resetCode, currentTime);
                
                mainHandler.post(() -> {
                    if (isValid) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Mã xác thực không đúng hoặc đã hết hạn");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error verifying password reset code", e);
                mainHandler.post(() -> callback.onError("Lỗi xác thực mã reset"));
            }
        });
    }
    
    /**
     * Cập nhật mật khẩu mới
     */
    public void updatePassword(String email, String newPassword, UpdatePasswordCallback callback) {
        executorService.execute(() -> {
            try {
                // Sử dụng plain text password
                boolean success = databaseApiService.updatePassword(email, newPassword);
                
                mainHandler.post(() -> {
                    if (success) {
                        // Clear reset code sau khi update thành công
                        databaseApiService.clearVerificationCode(email);
                        callback.onSuccess();
                    } else {
                        callback.onError("Không thể cập nhật mật khẩu");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error updating password", e);
                mainHandler.post(() -> callback.onError("Lỗi cập nhật mật khẩu"));
            }
        });
    }
    
    /**
     * Lấy tên đầy đủ của user theo email
     */
    public void getUserFullName(String email, UserInfoCallback callback) {
        executorService.execute(() -> {
            try {
                User user = databaseApiService.getUserByEmail(email);
                
                mainHandler.post(() -> {
                    if (user != null) {
                        callback.onSuccess(user.getFullName());
                    } else {
                        callback.onError("Không tìm thấy user");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error getting user full name", e);
                mainHandler.post(() -> callback.onError("Lỗi lấy thông tin user"));
            }
        });
    }
    
    /**
     * Admin đổi mật khẩu của user khác
     */
    public void adminChangeUserPassword(String userEmail, String newPassword, UpdatePasswordCallback callback) {
        Log.d(TAG, "=== ADMIN CHANGE PASSWORD START ===");
        Log.d(TAG, "Changing password for user: " + userEmail);
        Log.d(TAG, "New password length: " + newPassword.length());
        
        executorService.execute(() -> {
            try {
                // Sử dụng plain text password
                Log.d(TAG, "Calling databaseApiService.updatePassword");
                boolean success = databaseApiService.updatePassword(userEmail, newPassword);
                Log.d(TAG, "Password update result: " + success);
                
                mainHandler.post(() -> {
                    if (success) {
                        Log.d(TAG, "Password update successful, clearing verification code");
                        // Clear verification code sau khi update thành công
                        databaseApiService.clearVerificationCode(userEmail);
                        Log.d(TAG, "=== ADMIN CHANGE PASSWORD END - SUCCESS ===");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "Password update failed");
                        Log.d(TAG, "=== ADMIN CHANGE PASSWORD END - FAILED ===");
                        callback.onError("Không thể cập nhật mật khẩu cho user");
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error admin changing user password", e);
                Log.d(TAG, "=== ADMIN CHANGE PASSWORD END - ERROR ===");
                mainHandler.post(() -> callback.onError("Lỗi đổi mật khẩu user"));
            }
        });
    }
}
