package com.example.projectprm392.debug;

import android.content.Context;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.utils.DatabaseUtils;

/**
 * Helper class để debug và reset database khi cần thiết
 */
public class DatabaseResetHelper {
    
    public static void resetAndInitializeDatabase(Context context) {
        try {
            System.out.println("=== RESETTING DATABASE ===");
            
            // Reset database
            DatabaseUtils.resetDatabase(context);
            
            // Wait a bit
            Thread.sleep(1000);
            
            // Initialize with fresh data
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.initializeSampleData();
            
            System.out.println("=== DATABASE RESET COMPLETED ===");
            System.out.println("Admin credentials: admin@gmail.com / 123456");
            System.out.println("Test worker: test@gmail.com / 123456");
            System.out.println("Test employer: employer@gmail.com / 123456");
            
        } catch (Exception e) {
            System.err.println("Database reset failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
