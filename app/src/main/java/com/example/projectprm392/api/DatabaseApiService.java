package com.example.projectprm392.api;

import android.content.Context;

import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.LoginRequest;
import com.example.projectprm392.models.LoginResponse;
import com.example.projectprm392.models.User;

import java.util.UUID;

/**
 * Database API Service sử dụng SQLite database
 * Thay thế MockApiService để sử dụng database thực tế
 */
public class DatabaseApiService {
    
    private DatabaseHelper databaseHelper;
    
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
        
        // Lưu user vào database
        try {
            long userId = databaseHelper.insertUser(user);
            
            if (userId > 0) {
                LoginResponse response = new LoginResponse(true, "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
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
}
