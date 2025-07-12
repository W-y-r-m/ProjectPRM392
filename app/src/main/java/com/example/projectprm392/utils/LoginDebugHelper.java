package com.example.projectprm392.utils;

import android.content.Context;
import android.util.Log;

import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;

import java.util.List;

/**
 * Debug helper để kiểm tra login/database issues
 */
public class LoginDebugHelper {
    
    private static final String TAG = "LoginDebugHelper";
    
    /**
     * Kiểm tra tổng quan về user và session
     */
    public static void debugLoginIssue(Context context, String email, String password) {
        Log.d(TAG, "=== LOGIN DEBUG START ===");
        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Password: " + password);
        
        // 1. Kiểm tra SessionManager
        SessionManager sessionManager = new SessionManager(context);
        Log.d(TAG, "Is currently logged in: " + sessionManager.isLoggedIn());
        Log.d(TAG, "Session email: " + sessionManager.getEmail());
        Log.d(TAG, "Session user ID: " + sessionManager.getUserId());
        
        // 2. Kiểm tra Database
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        
        // Kiểm tra user có tồn tại không
        UserEntity userEntity = dbHelper.getUserByEmail(email);
        if (userEntity != null) {
            Log.d(TAG, "✅ User found in database");
            Log.d(TAG, "DB User ID: " + userEntity.getId());
            Log.d(TAG, "DB Email: " + userEntity.getEmail());
            Log.d(TAG, "DB Password: " + userEntity.getPassword());
            Log.d(TAG, "DB Is Verified: " + userEntity.getIsVerified());
            Log.d(TAG, "DB Role: " + userEntity.getRole());
            Log.d(TAG, "DB Post Quota: " + userEntity.getPostQuota());
            
            // Test password matching (plain text only)
            boolean plainMatch = password.equals(userEntity.getPassword());
            
            Log.d(TAG, "Password plain match: " + plainMatch);
            Log.d(TAG, "DB password: " + userEntity.getPassword());
            Log.d(TAG, "Input password: " + password);
            
        } else {
            Log.d(TAG, "❌ User NOT found in database");
            
            // List all users to see what's in DB
            List<UserEntity> allUsers = dbHelper.getAllUsers();
            Log.d(TAG, "Total users in DB: " + allUsers.size());
            
            for (int i = 0; i < Math.min(3, allUsers.size()); i++) {
                UserEntity u = allUsers.get(i);
                Log.d(TAG, "User " + (i+1) + ": " + u.getEmail() + " (ID: " + u.getId() + ")");
            }
        }
        
        Log.d(TAG, "=== LOGIN DEBUG END ===");
    }
    
    /**
     * Kiểm tra tại sao login thành công nhưng user không có trong DB
     */
    public static void debugSessionVsDatabase(Context context) {
        Log.d(TAG, "=== SESSION VS DATABASE DEBUG ===");
        
        SessionManager sessionManager = new SessionManager(context);
        
        if (sessionManager.isLoggedIn()) {
            String sessionEmail = sessionManager.getEmail();
            Log.d(TAG, "Session shows logged in as: " + sessionEmail);
            
            // Check if this user actually exists in database
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            UserEntity user = dbHelper.getUserByEmail(sessionEmail);
            
            if (user != null) {
                Log.d(TAG, "✅ Session user exists in database");
                Log.d(TAG, "DB User ID: " + user.getId());
                Log.d(TAG, "Session User ID: " + sessionManager.getUserId());
            } else {
                Log.d(TAG, "❌ Session user DOES NOT exist in database!");
                Log.d(TAG, "This means user logged in but data not persisted");
                
                // List current database users
                List<UserEntity> allUsers = dbHelper.getAllUsers();
                Log.d(TAG, "Current database users:");
                for (UserEntity u : allUsers) {
                    Log.d(TAG, "- " + u.getEmail() + " (verified: " + u.getIsVerified() + ")");
                }
            }
        } else {
            Log.d(TAG, "No active session");
        }
        
        Log.d(TAG, "=== END SESSION VS DATABASE DEBUG ===");
    }
}
