package com.example.projectprm392.utils;

import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * PayOS Debug utility để test kết nối
 */
public class PayOSDebugUtil {
    private static final String TAG = "PayOSDebug";
    
    /**
     * Test basic connectivity to PayOS domain
     */
    public static void testPayOSDomain() {
        new Thread(() -> {
            try {
                Log.d(TAG, "=== TESTING PAYOS DOMAIN ===");
                
                // Test CORRECT domain
                String correctDomain = "https://api-merchant.payos.vn";
                URL url = new URL(correctDomain);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "✅ CORRECT domain " + correctDomain + " response: " + responseCode);
                conn.disconnect();
                
                // Test old WRONG domain to show it fails
                try {
                    String wrongDomain = "https://api.payos.vn";
                    URL wrongUrl = new URL(wrongDomain);
                    HttpURLConnection wrongConn = (HttpURLConnection) wrongUrl.openConnection();
                    wrongConn.setRequestMethod("GET");
                    wrongConn.setConnectTimeout(5000);
                    wrongConn.setReadTimeout(5000);
                    
                    int wrongResponseCode = wrongConn.getResponseCode();
                    Log.d(TAG, "❌ WRONG domain " + wrongDomain + " response: " + wrongResponseCode);
                    wrongConn.disconnect();
                } catch (Exception e) {
                    Log.d(TAG, "❌ WRONG domain failed as expected: " + e.getMessage());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Domain test failed", e);
            }
        }).start();
    }
}
