package com.example.projectprm392.services;

import android.content.Context;
import android.util.Log;

import com.example.projectprm392.config.PayOSConfig;
import com.example.projectprm392.models.Payment;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PayOSService {
    private static final String TAG = "PayOSService";
    private static PayOSService instance;
    private Context context;
    private List<Payment> payments = new ArrayList<>();

    private PayOSService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized PayOSService getInstance(Context context) {
        if (instance == null) {
            instance = new PayOSService(context);
        }
        return instance;
    }

    /**
     * Tạo payment link cho PayOS
     */
    public CompletableFuture<Payment> createPaymentLink(UUID userId, int amount, String description) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Tạo payment object
                Payment payment = new Payment(
                    userId, 
                    PayOSConfig.DEFAULT_POST_QUOTA, 
                    BigDecimal.valueOf(amount),
                    description
                );

                // Tạo order code unique (sử dụng timestamp nhưng ngắn hơn để tránh lỗi)
                long timestamp = System.currentTimeMillis() / 1000;
                String orderCode = String.valueOf(timestamp);
                payment.setOrderCode(orderCode);

                Log.d(TAG, "=== CREATING REAL PAYOS PAYMENT ===");
                Log.d(TAG, "OrderCode: " + orderCode);
                Log.d(TAG, "Amount: " + amount);
                Log.d(TAG, "Description: " + description);
                
                // ONLY use real PayOS API - NO FALLBACK TO MOCK
                return createRealPayOSPayment(payment, orderCode, amount, description);

            } catch (Exception e) {
                Log.e(TAG, "=== ERROR CREATING PAYOS PAYMENT ===");
                Log.e(TAG, "Exception: " + e.getClass().getSimpleName());
                Log.e(TAG, "Message: " + e.getMessage());
                e.printStackTrace();
                
                // Re-throw exception instead of falling back to mock
                throw new RuntimeException("PayOS API Error: " + e.getMessage(), e);
            }
        });
    }
    
    private Payment createRealPayOSPayment(Payment payment, String orderCode, int amount, String description) throws Exception {
        // Tạo JSON payload cho PayOS theo đúng format API v2
        JSONObject payload = new JSONObject();
        
        // PayOS yêu cầu orderCode là số nguyên dương
        long orderCodeLong = Long.parseLong(orderCode);
        payload.put("orderCode", orderCodeLong);
        payload.put("amount", amount);
        payload.put("description", description);
        
        // Items array (required by PayOS)
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("name", "Nạp tiền mua lượt đăng tin");
        item.put("quantity", 1);
        item.put("price", amount);
        items.put(item);
        payload.put("items", items);
        
        payload.put("returnUrl", PayOSConfig.SUCCESS_URL);
        payload.put("cancelUrl", PayOSConfig.CANCEL_URL);

        Log.d(TAG, "=== PAYOS API REQUEST ===");
        Log.d(TAG, "PayOS Payload: " + payload.toString());
        Log.d(TAG, "API URL: " + PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
        Log.d(TAG, "Client ID: " + PayOSConfig.CLIENT_ID);
        Log.d(TAG, "API Key: " + PayOSConfig.API_KEY.substring(0, 8) + "...");
        
        // Call PayOS API với headers đúng theo documentation
        URL url = new URL(PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
        conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Android-App/1.0");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(30000); // 30 seconds
        conn.setReadTimeout(30000); // 30 seconds

        // Send request
        String jsonString = payload.toString();
        Log.d(TAG, "Sending JSON: " + jsonString);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
        }

        // Read response
        int responseCode = conn.getResponseCode();
        String responseMessage = conn.getResponseMessage();
        
        Log.d(TAG, "=== PAYOS API RESPONSE ===");
        Log.d(TAG, "Response Code: " + responseCode);
        Log.d(TAG, "Response Message: " + responseMessage);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        String responseBody = response.toString();
        Log.d(TAG, "Response Body: " + responseBody);

        if (responseCode == 200 || responseCode == 201) {
            try {
                JSONObject responseJson = new JSONObject(responseBody);
                
                // Check if response has success code
                if (responseJson.has("code") && responseJson.getInt("code") == 0) {
                    JSONObject data = responseJson.getJSONObject("data");
                    
                    // Update payment với thông tin từ PayOS
                    String payosOrderId = data.getString("orderCode");
                    String qrCodeUrl = data.getString("qrCode");
                    String checkoutUrl = data.optString("checkoutUrl", "");
                    
                    payment.setPayosOrderId(payosOrderId);
                    payment.setQrCodeUrl(qrCodeUrl);
                    payment.setStatus(Payment.PaymentStatus.PENDING);
                    
                    // Lưu payment vào database (mock)
                    payments.add(payment);
                    
                    Log.d(TAG, "✅ PayOS payment created successfully!");
                    Log.d(TAG, "PayOS Order Code: " + payosOrderId);
                    Log.d(TAG, "QR URL: " + qrCodeUrl);
                    Log.d(TAG, "Checkout URL: " + checkoutUrl);
                    return payment;
                } else {
                    String errorCode = responseJson.optString("code", "unknown");
                    String errorDesc = responseJson.optString("desc", "Unknown error");
                    String errorMsg = "PayOS API Error - Code: " + errorCode + ", Message: " + errorDesc;
                    Log.e(TAG, errorMsg);
                    throw new RuntimeException(errorMsg);
                }
            } catch (Exception jsonException) {
                Log.e(TAG, "Error parsing PayOS response JSON", jsonException);
                throw new RuntimeException("Invalid PayOS response format: " + responseBody);
            }
        } else {
            String errorMsg = "HTTP Error " + responseCode + " " + responseMessage + ": " + responseBody;
            Log.e(TAG, errorMsg);
            
            // Try to parse error response
            try {
                JSONObject errorJson = new JSONObject(responseBody);
                String errorDesc = errorJson.optString("desc", errorJson.optString("message", "Unknown error"));
                throw new RuntimeException("PayOS API Error: " + errorDesc);
            } catch (Exception jsonException) {
                throw new RuntimeException(errorMsg);
            }
        }
    }

    /**
     * Kiểm tra trạng thái payment từ PayOS API thực tế
     */
    public CompletableFuture<Payment> getPaymentStatus(String orderCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Tìm payment trong database
                Payment payment = findPaymentByOrderCode(orderCode);
                if (payment == null) {
                    throw new RuntimeException("Payment not found in local database");
                }

                Log.d(TAG, "=== CHECKING PAYOS PAYMENT STATUS ===");
                Log.d(TAG, "Order Code: " + orderCode);
                
                String apiUrl = PayOSConfig.PAYOS_BASE_URL + 
                    PayOSConfig.GET_PAYMENT_INFO.replace("{orderCode}", orderCode);
                
                Log.d(TAG, "API URL: " + apiUrl);
                
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
                conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();
                String responseMessage = conn.getResponseMessage();
                
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }

                String responseBody = response.toString();
                Log.d(TAG, "=== PAYOS STATUS RESPONSE ===");
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response Message: " + responseMessage);
                Log.d(TAG, "Response Body: " + responseBody);

                if (responseCode == 200) {
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        
                        if (responseJson.has("code") && responseJson.getInt("code") == 0) {
                            JSONObject data = responseJson.getJSONObject("data");
                            String status = data.getString("status");
                            
                            Log.d(TAG, "PayOS Payment Status: " + status);
                            updatePaymentStatus(payment, status);
                            return payment;
                        } else {
                            String errorCode = responseJson.optString("code", "unknown");
                            String errorDesc = responseJson.optString("desc", "Unknown error");
                            Log.e(TAG, "PayOS Status API Error - Code: " + errorCode + ", Message: " + errorDesc);
                            // Return current payment status instead of throwing error
                            return payment;
                        }
                    } catch (Exception jsonException) {
                        Log.e(TAG, "Error parsing PayOS status response", jsonException);
                        return payment;
                    }
                } else {
                    Log.e(TAG, "PayOS Status HTTP Error: " + responseCode + " " + responseMessage);
                    Log.e(TAG, "Error Response: " + responseBody);
                    // Return current payment status instead of throwing error
                    return payment;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error checking PayOS payment status", e);
                // Return payment hiện tại thay vì throw exception
                Payment payment = findPaymentByOrderCode(orderCode);
                return payment != null ? payment : null;
            }
        });
    }

    /**
     * Xử lý khi payment thành công
     */
    public boolean processSuccessfulPayment(String orderCode, UUID userId) {
        try {
            Payment payment = findPaymentByOrderCode(orderCode);
            if (payment != null && payment.getUserId().equals(userId)) {
                // Cập nhật trạng thái payment
                payment.markAsCompleted();
                
                // Cập nhật post quota cho user
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                UserEntity user = databaseHelper.getUserById(userId.toString());
                if (user != null) {
                    // Trừ 10 từ postQuota (logic ban đầu: từ 20 về 10)
                    user.reducePostQuotaByPayment(10);
                    databaseHelper.updateUser(user);
                    
                    Log.d(TAG, "Updated user post quota: " + user.getPostQuota());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error processing successful payment", e);
            return false;
        }
    }

    // Helper methods
    private String createSignature(JSONObject payload) {
        try {
            // Simplified signature creation - in real app should follow PayOS documentation
            String data = payload.toString();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                PayOSConfig.CHECKSUM_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error creating signature", e);
            return "";
        }
    }

    private Payment findPaymentByOrderCode(String orderCode) {
        return payments.stream()
                .filter(p -> orderCode.equals(p.getOrderCode()))
                .findFirst()
                .orElse(null);
    }

    private void updatePaymentStatus(Payment payment, String payosStatus) {
        switch (payosStatus) {
            case "PAID":
                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                break;
            case "CANCELLED":
            case "EXPIRED":
                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                break;
            default:
                payment.setStatus(Payment.PaymentStatus.PENDING);
                break;
        }
    }

    // Mock database methods
    public List<Payment> getPaymentsByUserId(UUID userId) {
        return payments.stream()
                .filter(p -> userId.equals(p.getUserId()))
                .collect(java.util.stream.Collectors.toList());
    }

    public Payment getPaymentById(UUID paymentId) {
        return payments.stream()
                .filter(p -> paymentId.equals(p.getPaymentId()))
                .findFirst()
                .orElse(null);
    }
}
