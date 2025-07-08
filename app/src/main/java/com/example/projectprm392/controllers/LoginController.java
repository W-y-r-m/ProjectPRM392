package com.example.projectprm392.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.projectprm392.api.DatabaseApiService;
import com.example.projectprm392.models.LoginRequest;
import com.example.projectprm392.models.LoginResponse;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.utils.ValidationUtils;

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
}
