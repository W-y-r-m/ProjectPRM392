package com.example.projectprm392.utils;

import android.content.Context;
import com.example.projectprm392.database.AppDatabase;

/**
 * Utility class để handle database issues
 */
public class DatabaseUtils {
    
    /**
     * Reset database hoàn toàn khi có vấn đề
     */
    public static void resetDatabase(Context context) {
        try {
            AppDatabase database = AppDatabase.getDatabase(context);
            if (database.isOpen()) {
                database.close();
            }
            
            // Destroy instance
            AppDatabase.destroyInstance();
            
            // Delete database file
            context.deleteDatabase("app_database");
            
            System.out.println("Database reset successfully");
        } catch (Exception e) {
            System.err.println("Error resetting database: " + e.getMessage());
        }
    }
    
    /**
     * Check database health
     */
    public static boolean isDatabaseHealthy(Context context) {
        try {
            AppDatabase database = AppDatabase.getDatabase(context);
            // Try to access a simple query
            database.userDao().getAll();
            return true;
        } catch (Exception e) {
            System.err.println("Database health check failed: " + e.getMessage());
            return false;
        }
    }
}
