package com.example.projectprm392.debug;

import android.content.Context;
import android.util.Log;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import java.util.List;

/**
 * Helper để debug admin login issues
 */
public class AdminLoginDebugger {
    private static final String TAG = "AdminLoginDebugger";
    
    public static void debugAdminAccount(Context context) {
        try {
            DatabaseHelper db = new DatabaseHelper(context);
            
            Log.d(TAG, "=== DEBUGGING ADMIN ACCOUNT ===");
            
            // Check if any users exist
            List<UserEntity> allUsers = db.getAllUsers();
            Log.d(TAG, "Total users in database: " + allUsers.size());
            
            // Look for admin specifically
            UserEntity adminByEmail = db.getUserByEmail("admin@gmail.com");
            if (adminByEmail != null) {
                Log.d(TAG, "✅ Found admin by email:");
                Log.d(TAG, "  - Email: " + adminByEmail.getEmail());
                Log.d(TAG, "  - Password: " + adminByEmail.getPassword());
                Log.d(TAG, "  - Role: " + adminByEmail.getRole());
                Log.d(TAG, "  - Verified: " + adminByEmail.getIsVerified());
                Log.d(TAG, "  - FullName: " + adminByEmail.getFullName());
            } else {
                Log.e(TAG, "❌ Admin account NOT FOUND by email");
            }
            
            // Try login combination
            UserEntity loginTest = db.getUserByEmailAndPassword("admin@gmail.com", "123456");
            if (loginTest != null) {
                Log.d(TAG, "✅ Login test SUCCESSFUL");
            } else {
                Log.e(TAG, "❌ Login test FAILED");
            }
            
            // List all users for debugging
            Log.d(TAG, "=== ALL USERS ===");
            for (UserEntity user : allUsers) {
                Log.d(TAG, "User: " + user.getEmail() + " | Role: " + user.getRole() + " | Verified: " + user.getIsVerified());
            }
            
            Log.d(TAG, "=== DEBUG COMPLETE ===");
            
        } catch (Exception e) {
            Log.e(TAG, "Debug failed: " + e.getMessage(), e);
        }
    }
}
