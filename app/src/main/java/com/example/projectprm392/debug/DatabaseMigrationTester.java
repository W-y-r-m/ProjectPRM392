package com.example.projectprm392.debug;

import android.content.Context;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.DatabaseUtils;

import java.util.List;

/**
 * Test class để debug database migration issues
 */
public class DatabaseMigrationTester {
    
    public static void testDatabaseMigration(Context context) {
        System.out.println("=== DATABASE MIGRATION TEST ===");
        
        try {
            // Check database health first
            boolean isHealthy = DatabaseUtils.isDatabaseHealthy(context);
            System.out.println("Database healthy: " + isHealthy);
            
            if (!isHealthy) {
                System.out.println("Database unhealthy, resetting...");
                DatabaseUtils.resetDatabase(context);
                
                // Wait a bit and check again
                Thread.sleep(1000);
                isHealthy = DatabaseUtils.isDatabaseHealthy(context);
                System.out.println("After reset, database healthy: " + isHealthy);
            }
            
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            
            // Force sample data creation
            databaseHelper.initializeSampleData();
            
            // Test user retrieval
            List<UserEntity> allUsers = databaseHelper.getAllUsers();
            System.out.println("Total users: " + allUsers.size());
            
            for (UserEntity userEntity : allUsers) {
                System.out.println("Testing user: " + userEntity.getFullName());
                System.out.println("  - Phone: " + userEntity.getPhoneNumber());
                System.out.println("  - UserId: " + userEntity.getUserId());
                
                // Test conversion
                User convertedUser = databaseHelper.convertEntityToUser(userEntity);
                if (convertedUser != null) {
                    System.out.println("  ✅ Conversion successful");
                    System.out.println("  - Converted phone: " + convertedUser.getPhoneNumber());
                } else {
                    System.out.println("  ❌ Conversion failed");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== TEST COMPLETED ===");
    }
}
