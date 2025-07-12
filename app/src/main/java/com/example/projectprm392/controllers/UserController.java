package com.example.projectprm392.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.projectprm392.api.DatabaseApiService;
import com.example.projectprm392.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserController {
    private static final String TAG = "UserController";
    
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }
    
    private DatabaseApiService databaseApiService;
    private Context context;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public UserController(Context context) {
        this.context = context;
        this.databaseApiService = new DatabaseApiService(context);
        this.executorService = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void getUserById(String userId, UserCallback callback) {
        executorService.execute(() -> {
            try {
                User user = databaseApiService.getUserById(userId);
                if (user != null) {
                    mainHandler.post(() -> callback.onSuccess(user));
                } else {
                    mainHandler.post(() -> callback.onError("Không tìm thấy người dùng"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by ID", e);
                mainHandler.post(() -> callback.onError("Lỗi khi lấy thông tin người dùng: " + e.getMessage()));
            }
        });
    }
    
    public void updateUser(User user, UserCallback callback) {
        executorService.execute(() -> {
            try {
                User updatedUser = databaseApiService.updateUser(user);
                if (updatedUser != null) {
                    mainHandler.post(() -> callback.onSuccess(updatedUser));
                } else {
                    mainHandler.post(() -> callback.onError("Cập nhật thất bại"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating user", e);
                mainHandler.post(() -> callback.onError("Lỗi khi cập nhật người dùng: " + e.getMessage()));
            }
        });
    }
    
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
