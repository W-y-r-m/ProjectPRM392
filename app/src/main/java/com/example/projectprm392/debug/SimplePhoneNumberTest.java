package com.example.projectprm392.debug;

import android.content.Context;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;

import java.util.List;

/**
 * Simple test để verify PhoneNumber functionality
 */
public class SimplePhoneNumberTest {
    
    public static void testPhoneNumberFeature(Context context) {
        System.out.println("=== SIMPLE PHONE NUMBER TEST ===");
        
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            
            // Initialize sample data
            databaseHelper.initializeSampleData();
            
            // Get all users
            List<UserEntity> users = databaseHelper.getAllUsers();
            System.out.println("Total users found: " + users.size());
            
            if (users.size() > 0) {
                UserEntity firstUser = users.get(0);
                System.out.println("First user: " + firstUser.getFullName());
                System.out.println("First user phone: " + firstUser.getPhoneNumber());
                System.out.println("First user ID: " + firstUser.getUserId());
                
                // Test getUserById
                UserEntity foundUser = databaseHelper.getUserById(firstUser.getUserId());
                if (foundUser != null) {
                    System.out.println("✅ getUserById works");
                    System.out.println("Found user phone: " + foundUser.getPhoneNumber());
                    
                    // Test conversion
                    User convertedUser = databaseHelper.convertEntityToUser(foundUser);
                    if (convertedUser != null) {
                        System.out.println("✅ Conversion works");
                        System.out.println("Converted user phone: " + convertedUser.getPhoneNumber());
                    } else {
                        System.out.println("❌ Conversion failed");
                    }
                } else {
                    System.out.println("❌ getUserById failed");
                }
            } else {
                System.out.println("❌ No users found in database");
            }
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== TEST COMPLETED ===");
    }
}
