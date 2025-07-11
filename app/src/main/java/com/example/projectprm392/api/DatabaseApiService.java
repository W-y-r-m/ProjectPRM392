package com.example.projectprm392.api;

import android.content.Context;
import android.util.Log;

import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.viewmodels.LoginRequest;
import com.example.projectprm392.viewmodels.LoginResponse;
import com.example.projectprm392.models.User;

import java.util.Date;
import java.util.UUID;

/**
 * Database API Service sử dụng SQLite database
 * Thay thế MockApiService để sử dụng database thực tế
 */
public class DatabaseApiService {
    
    private DatabaseHelper databaseHelper;
    private static final String TAG = "DatabaseApiService";
    
    public DatabaseApiService(Context context) {
        databaseHelper = new DatabaseHelper(context);
        // Khởi tạo dữ liệu mẫu
        databaseHelper.initializeSampleData();
    }
    
    public LoginResponse login(LoginRequest request) {
        // Simulate network delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Tìm user trong database
        UserEntity userEntity = databaseHelper.getUserByEmailAndPassword(
            request.getEmail(), 
            request.getPassword()
        );
        
        if (userEntity != null) {
            // Kiểm tra xem user đã verify chưa
            if (!userEntity.getIsVerified()) {
                return new LoginResponse(false, "Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
            }
            
            // Chuyển đổi entity sang user model
            User user = databaseHelper.convertEntityToUser(userEntity);
            
            LoginResponse response = new LoginResponse(true, "Đăng nhập thành công");
            response.setUser(user);
            response.setToken("db_token_" + UUID.randomUUID().toString());
            return response;
        } else {
            // Kiểm tra xem email có tồn tại không
            if (databaseHelper.isEmailExists(request.getEmail())) {
                return new LoginResponse(false, "Mật khẩu không chính xác");
            } else {
                return new LoginResponse(false, "Tài khoản không tồn tại. Vui lòng đăng ký.");
            }
        }
    }
    
    public LoginResponse register(User user) {
        // Simulate network delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Kiểm tra email đã tồn tại chưa
        if (databaseHelper.isEmailExists(user.getEmail())) {
            return new LoginResponse(false, "Email đã được sử dụng");
        }
        
        // Lưu user vào database với is_verified = false
        try {
            // Set user as not verified initially
            user.setVerified(false);
            long userId = databaseHelper.insertUser(user);
            
            if (userId > 0) {
                LoginResponse response = new LoginResponse(true, "Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.");
                response.setUser(user);
                response.setToken("db_token_" + UUID.randomUUID().toString());
                return response;
            } else {
                return new LoginResponse(false, "Đăng ký thất bại. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            return new LoginResponse(false, "Lỗi hệ thống: " + e.getMessage());
        }
    }
    
    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
    
    public void updateVerificationCode(String email, String code, Date expiresAt) {
        try {
            databaseHelper.updateVerificationCode(email, code, expiresAt);
        } catch (Exception e) {
            Log.e(TAG, "Error updating verification code", e);
            throw e;
        }
    }
    
    public boolean verifyUser(String email, String code, Date currentTime) {
        try {
            boolean isVerified = databaseHelper.verifyUser(email, code, currentTime);
            return isVerified;
        } catch (Exception e) {
            Log.e(TAG, "Error verifying user", e);
            return false;
        }
    }
    
    /**
     * Cập nhật mật khẩu cho user
     */
    public boolean updatePassword(String email, String hashedPassword) {
        try {
            boolean success = databaseHelper.updatePassword(email, hashedPassword);
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
            return false;
        }
    }
    
    /**
     * Xóa verification code sau khi sử dụng
     */
    public void clearVerificationCode(String email) {
        try {
            databaseHelper.clearVerificationCode(email);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing verification code", e);
        }
    }
    
    /**
     * Lấy user theo email
     */
    public User getUserByEmail(String email) {
        try {
            UserEntity userEntity = databaseHelper.getUserByEmail(email);
            
            if (userEntity != null) {
                User user = new User();
                user.setUserId(UUID.fromString(userEntity.getUserId()));
                user.setEmail(userEntity.getEmail());
                user.setFullName(userEntity.getFullName());
                user.setRole(userEntity.getRole());
                user.setVerified(userEntity.getIsVerified());
                return user;
            }
            
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by email", e);
            return null;
        }
    }
}
