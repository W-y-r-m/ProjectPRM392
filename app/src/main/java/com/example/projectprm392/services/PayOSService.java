package com.example.projectprm392.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.example.projectprm392.config.PayOSConfig;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.Payment;

/**
 * PayOS Service - Chỉ tích hợp PayOS API thật, không có mock fallback
 */
public class PayOSService {
    private static final String TAG = "PayOSService";
    private static PayOSService instance;
    private final Context context;
    private final List<Payment> payments = new ArrayList<>();

    private PayOSService(Context context) {
        this.context = context.getApplicationContext();
        Log.d(TAG, "PayOSService initialized - REAL PayOS API only, no mock fallback");
    }

    /**
     * Singleton pattern để tránh null instance
     */
    public static synchronized PayOSService getInstance(Context context) {
        if (instance == null) {
            instance = new PayOSService(context);
        }
        return instance;
    }

    /**
     * Kiểm tra kết nối mạng
     */
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = 
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
                Log.d(TAG, "Network status: " + (isConnected ? "✅ CONNECTED" : "❌ DISCONNECTED"));
                return isConnected;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking network status", e);
        }
        return false;
    }

    /**
     * Tạo payment link PayOS - KHÔNG có fallback mock
     */
    public CompletableFuture<Payment> createPaymentLink(UUID userId, int amount, String description) {
        final UUID finalUserId = userId;
        final int finalAmount = amount;
        final String finalDescription = (description == null || description.trim().isEmpty()) ? "Nap post quota" : description;
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Kiểm tra kết nối mạng trước
                if (!isNetworkAvailable()) {
                    throw new RuntimeException("Không có kết nối mạng. Vui lòng kiểm tra kết nối và thử lại.");
                }

                // Validate inputs
                if (finalUserId == null) {
                    throw new IllegalArgumentException("User ID không được null");
                }
                if (finalAmount <= 0) {
                    throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
                }

                // Validate PayOS configuration
                if (!isPayOSConfigValid()) {
                    String configError = "PayOS configuration không hợp lệ - kiểm tra CLIENT_ID, API_KEY, CHECKSUM_KEY";
                    Log.e(TAG, "❌ " + configError);
                    throw new RuntimeException(configError);
                }

                // Tạo payment object
                Payment payment = new Payment(
                    finalUserId, 
                    PayOSConfig.DEFAULT_POST_QUOTA, 
                    BigDecimal.valueOf(finalAmount),
                    finalDescription
                );

                // Tạo orderCode unique để tránh PayOS Error Code 201
                long currentTime = System.currentTimeMillis();
                int randomSuffix = (int) (Math.random() * 999) + 1;
                String orderCode = String.valueOf(currentTime / 1000) + String.format("%03d", randomSuffix);
                
                // Giới hạn độ dài orderCode theo PayOS requirement
                if (orderCode.length() > 12) {
                    orderCode = orderCode.substring(orderCode.length() - 12);
                }
                
                payment.setOrderCode(orderCode);
                
                Log.d(TAG, "=== CREATING PAYOS PAYMENT ===");
                Log.d(TAG, "OrderCode: " + orderCode);
                Log.d(TAG, "Amount: " + finalAmount);
                Log.d(TAG, "Description: " + finalDescription);
                
                // Call real PayOS API with improved retry logic
                Payment realPayment = createRealPayOSPayment(payment, orderCode, finalAmount, finalDescription);
                Log.d(TAG, "✅ Successfully created PayOS payment!");
                Log.d(TAG, "QR URL: " + realPayment.getQrCodeUrl());
                return realPayment;

            } catch (Exception e) {
                Log.e(TAG, "=== PAYMENT CREATION FAILED ===");
                Log.e(TAG, "Exception: " + e.getClass().getSimpleName());
                Log.e(TAG, "Message: " + e.getMessage());
                e.printStackTrace();
                
                throw new RuntimeException("Không thể tạo PayOS payment: " + e.getMessage(), e);
            }
        });
    }
    
    private Payment createRealPayOSPayment(Payment payment, String orderCode, int amount, String description) throws Exception {
        Exception lastException = null;
        int maxRetries = 3;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Log.d(TAG, "=== PAYOS API ATTEMPT " + attempt + "/" + maxRetries + " ===");
                
                // Kiểm tra lại kết nối mạng trước mỗi attempt
                if (!isNetworkAvailable()) {
                    throw new RuntimeException("Mất kết nối mạng trong attempt " + attempt);
                }
                
                return attemptPayOSRequest(payment, orderCode, amount, description);
            } catch (Exception e) {
                lastException = e;
                Log.e(TAG, "Attempt " + attempt + " failed: " + e.getMessage());
                
                // Don't retry configuration errors
                if (e.getMessage() != null && 
                    (e.getMessage().contains("Unauthorized") || 
                     e.getMessage().contains("Code 12") ||
                     e.getMessage().contains("Invalid signature") ||
                     e.getMessage().contains("401") ||
                     e.getMessage().contains("403"))) {
                    Log.e(TAG, "❌ Configuration error detected, not retrying");
                    throw e;
                }
                
                // Không retry cho network timeout quá nhiều lần
                if (e.getMessage() != null && 
                    (e.getMessage().contains("timeout") || 
                     e.getMessage().contains("ConnectException") ||
                     e.getMessage().contains("UnknownHostException")) && 
                    attempt >= 2) {
                    Log.e(TAG, "❌ Network error after 2 attempts, stopping");
                    throw new RuntimeException("Không thể kết nối đến PayOS server. Kiểm tra kết nối mạng.", e);
                }
                
                if (attempt < maxRetries) {
                    try {
                        long sleepTime = 3000 * attempt; // Tăng thời gian chờ
                        Log.d(TAG, "⏳ Waiting " + sleepTime + "ms before retry...");
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("PayOS API failed after " + maxRetries + " attempts. Last error: " + 
            (lastException != null ? lastException.getMessage() : "Unknown error"));
    }
    
    private Payment attemptPayOSRequest(Payment payment, String orderCode, int amount, String description) throws Exception {
        // Validate inputs
        if (amount <= 0 || amount > 50000000) { // PayOS limit
            throw new IllegalArgumentException("Amount must be between 1 and 50,000,000 VND");
        }
        
        // Tạo JSON payload theo PayOS API v2 specification
        JSONObject payload = new JSONObject();
        
        long orderCodeLong = Long.parseLong(orderCode);
        payload.put("orderCode", orderCodeLong);
        payload.put("amount", amount);
        
        // Clean description - PayOS có giới hạn ký tự
        String cleanDescription = description.replaceAll("[^a-zA-Z0-9\\s]", "");
        if (cleanDescription.length() > 25) {
            cleanDescription = cleanDescription.substring(0, 25);
        }
        cleanDescription = cleanDescription.trim();
        if (cleanDescription.isEmpty()) {
            cleanDescription = "Nap tien post quota";
        }
        payload.put("description", cleanDescription);
        
        // Items array - required by PayOS
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("name", "Post quota");
        item.put("quantity", 1);
        item.put("price", amount);
        items.put(item);
        payload.put("items", items);
        
        // Return URLs - validate format
        String returnUrl = PayOSConfig.SUCCESS_URL;
        String cancelUrl = PayOSConfig.CANCEL_URL;
        
        if (returnUrl == null || !returnUrl.startsWith("http")) {
            returnUrl = "https://example.com/success";
            Log.w(TAG, "⚠️ Using default return URL: " + returnUrl);
        }
        
        if (cancelUrl == null || !cancelUrl.startsWith("http")) {
            cancelUrl = "https://example.com/cancel";
            Log.w(TAG, "⚠️ Using default cancel URL: " + cancelUrl);
        }
        
        payload.put("returnUrl", returnUrl);
        payload.put("cancelUrl", cancelUrl);
        
        // Signature
        String signature = createPayOSSignature(payload);
        if (signature.isEmpty()) {
            throw new RuntimeException("Cannot create valid signature");
        }
        payload.put("signature", signature);

        Log.d(TAG, "=== PAYOS API REQUEST ===");
        Log.d(TAG, "OrderCode: " + orderCodeLong);
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "Description: '" + cleanDescription + "'");
        Log.d(TAG, "ReturnUrl: " + returnUrl);
        Log.d(TAG, "CancelUrl: " + cancelUrl);
        Log.d(TAG, "Signature: " + signature.substring(0, 16) + "...");
        Log.d(TAG, "Full payload: " + payload.toString());
        
        // Call PayOS API
        URL url = new URL(PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
        HttpURLConnection conn = null;
        String responseBody = "";
        
        try {
            conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
            conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Android-PayOS-Client/1.0");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            // Tăng timeout cho emulator
            conn.setConnectTimeout(45000); // 45 seconds
            conn.setReadTimeout(45000);    // 45 seconds
            
            // Không sử dụng cache
            conn.setUseCaches(false);
            
            Log.d(TAG, "=== HTTP REQUEST DETAILS ===");
            Log.d(TAG, "URL: " + url.toString());
            Log.d(TAG, "Method: POST");
            Log.d(TAG, "Headers: Content-Type=" + conn.getRequestProperty("Content-Type"));
            Log.d(TAG, "Connect Timeout: " + conn.getConnectTimeout() + "ms");
            Log.d(TAG, "Read Timeout: " + conn.getReadTimeout() + "ms");

            // Send request
            String jsonString = payload.toString();
            Log.d(TAG, "Request body length: " + jsonString.length() + " bytes");
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            // Read response
            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            
            Log.d(TAG, "=== HTTP RESPONSE DETAILS ===");
            Log.d(TAG, "Response Code: " + responseCode);
            Log.d(TAG, "Response Message: " + responseMessage);
            
            // Log response headers
            for (String headerName : conn.getHeaderFields().keySet()) {
                if (headerName != null) {
                    Log.d(TAG, "Header " + headerName + ": " + conn.getHeaderField(headerName));
                }
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                    StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            responseBody = response.toString();
            Log.d(TAG, "Response Body Length: " + responseBody.length() + " bytes");
            Log.d(TAG, "Response Body: " + responseBody);

        } catch (java.net.ConnectException e) {
            Log.e(TAG, "❌ Connection failed - server không phản hồi", e);
            throw new RuntimeException("Không thể kết nối đến PayOS server. Kiểm tra kết nối mạng.", e);
        } catch (java.net.SocketTimeoutException e) {
            Log.e(TAG, "❌ Request timeout - server phản hồi chậm", e);
            throw new RuntimeException("PayOS server phản hồi chậm. Thử lại sau.", e);
        } catch (java.net.UnknownHostException e) {
            Log.e(TAG, "❌ DNS resolution failed - không tìm thấy server", e);
            throw new RuntimeException("Không thể tìm thấy PayOS server. Kiểm tra DNS/Internet.", e);
        } catch (IOException e) {
            Log.e(TAG, "❌ IO Exception during PayOS API call", e);
            throw new RuntimeException("Lỗi mạng khi gọi PayOS API: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        // Parse response
        try {
            if (responseBody.trim().isEmpty()) {
                throw new RuntimeException("PayOS API trả về response rỗng");
            }
            
            Log.d(TAG, "=== PARSING PAYOS RESPONSE ===");
            Log.d(TAG, "Raw response: " + responseBody);
            
            JSONObject responseJson;
            try {
                responseJson = new JSONObject(responseBody);
            } catch (JSONException e) {
                Log.e(TAG, "❌ Invalid JSON response from PayOS", e);
                throw new RuntimeException("PayOS trả về dữ liệu không hợp lệ: " + e.getMessage());
            }
            
            String code = responseJson.optString("code", "");
            String desc = responseJson.optString("desc", "Unknown error");
            
            Log.d(TAG, "PayOS response code: " + code);
            Log.d(TAG, "PayOS response desc: " + desc);
            
            if ("00".equals(code)) {
                if (!responseJson.has("data")) {
                    Log.e(TAG, "❌ PayOS success response missing 'data' field");
                    throw new RuntimeException("PayOS response thiếu dữ liệu");
                }
                
                JSONObject data = responseJson.getJSONObject("data");
                
                Log.d(TAG, "=== PAYOS SUCCESS DATA ===");
                Log.d(TAG, "Available fields: " + data.keys().toString());
                Log.d(TAG, "Full data object: " + data.toString());
                
                // PayOS có thể trả về nhiều field khác nhau cho QR/Payment URL
                String qrCodeUrl = data.optString("qrCode", "");
                String checkoutUrl = data.optString("checkoutUrl", "");
                String paymentLinkId = data.optString("paymentLinkId", "");
                String bin = data.optString("bin", "");
                String accountNumber = data.optString("accountNumber", "");
                
                Log.d(TAG, "qrCode field: '" + qrCodeUrl + "'");
                Log.d(TAG, "checkoutUrl field: '" + checkoutUrl + "'");
                Log.d(TAG, "paymentLinkId field: '" + paymentLinkId + "'");
                Log.d(TAG, "bin field: '" + bin + "'");
                Log.d(TAG, "accountNumber field: '" + accountNumber + "'");
                
                // Chọn URL phù hợp theo thứ tự ưu tiên
                String finalQrUrl = "";
                String urlSource = "";
                
                if (!qrCodeUrl.isEmpty() && (qrCodeUrl.startsWith("http") || qrCodeUrl.startsWith("data:image/"))) {
                    finalQrUrl = qrCodeUrl;
                    urlSource = "qrCode";
                } else if (!checkoutUrl.isEmpty() && checkoutUrl.startsWith("http")) {
                    finalQrUrl = checkoutUrl;
                    urlSource = "checkoutUrl";
                } else if (!paymentLinkId.isEmpty()) {
                    finalQrUrl = paymentLinkId;
                    urlSource = "paymentLinkId";
                } else if (!qrCodeUrl.isEmpty()) {
                    // Fallback: sử dụng qrCode field dù không phải URL standard
                    finalQrUrl = qrCodeUrl;
                    urlSource = "qrCode (fallback)";
                }
                
                if (finalQrUrl.isEmpty()) {
                    Log.e(TAG, "❌ PayOS không trả về URL nào hợp lệ");
                    Log.e(TAG, "Available fields in data: " + data.keys().toString());
                    Log.e(TAG, "Full PayOS response: " + responseJson.toString());
                    throw new RuntimeException("PayOS không trả về URL thanh toán hợp lệ");
                }
                
                Log.d(TAG, "✅ Selected URL from '" + urlSource + "': " + finalQrUrl);
                
                // Validate và log detailed info về QR URL
                validateAndLogQrUrl(finalQrUrl);
                
                payment.setQrCodeUrl(finalQrUrl);
                payment.setStatus(Payment.PaymentStatus.PENDING);
                payments.add(payment);
                
                Log.d(TAG, "✅ PayOS payment created successfully!");
                return payment;
                
            } else {
                String errorMsg = "PayOS Error Code " + code + ": " + desc;
                
                // Special error handling cho các lỗi thường gặp
                switch (code) {
                    case "201":
                        errorMsg = "PayOS Error: OrderCode đã tồn tại - " + desc;
                        break;
                    case "12":
                        errorMsg = "PayOS Error: Chữ ký không đúng - kiểm tra CLIENT_ID, API_KEY, CHECKSUM_KEY";
                        break;
                    case "401":
                        errorMsg = "PayOS Error: Unauthorized - kiểm tra CLIENT_ID và API_KEY";
                        break;
                    case "403":
                        errorMsg = "PayOS Error: Forbidden - tài khoản PayOS bị hạn chế";
                        break;
                    case "400":
                        errorMsg = "PayOS Error: Bad Request - " + desc;
                        break;
                    default:
                        errorMsg = "PayOS Error Code " + code + ": " + desc;
                }
                
                Log.e(TAG, errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "❌ Failed to parse PayOS JSON response", e);
            Log.e(TAG, "Response body that failed to parse: " + responseBody);
            throw new RuntimeException("Invalid PayOS response format: " + e.getMessage());
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error parsing PayOS response", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    private String createPayOSSignature(JSONObject payload) {
        try {
            Log.d(TAG, "=== CREATING PAYOS SIGNATURE ===");
            
            // Validate required fields
            if (!payload.has("amount") || !payload.has("cancelUrl") || 
                !payload.has("description") || !payload.has("orderCode") || 
                !payload.has("returnUrl")) {
                Log.e(TAG, "❌ Missing required fields for signature");
                throw new IllegalArgumentException("Missing required fields for signature");
            }
            
            // Tạo signature string theo PayOS spec (alphabetical order)
            StringBuilder dataToSign = new StringBuilder();
            dataToSign.append("amount=").append(payload.getLong("amount"));
            dataToSign.append("&cancelUrl=").append(payload.getString("cancelUrl"));
            dataToSign.append("&description=").append(payload.getString("description"));
            dataToSign.append("&orderCode=").append(payload.getLong("orderCode"));
            dataToSign.append("&returnUrl=").append(payload.getString("returnUrl"));
            
            String dataString = dataToSign.toString();
            Log.d(TAG, "Data to sign: " + dataString);
            
            // Validate checksum key
            if (PayOSConfig.CHECKSUM_KEY == null || PayOSConfig.CHECKSUM_KEY.trim().isEmpty()) {
                Log.e(TAG, "❌ CHECKSUM_KEY is null or empty");
                throw new IllegalArgumentException("CHECKSUM_KEY is required for signature");
            }
            
            Log.d(TAG, "Using CHECKSUM_KEY: " + PayOSConfig.CHECKSUM_KEY.substring(0, 8) + "...");
            
            // HMAC SHA256
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                PayOSConfig.CHECKSUM_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(dataString.getBytes(StandardCharsets.UTF_8));
            
            // Convert to hex
            StringBuilder result = new StringBuilder();
            for (byte b : hash) {
                result.append(String.format("%02x", b));
            }
            
            String signature = result.toString();
            Log.d(TAG, "Generated signature: " + signature.substring(0, 16) + "...");
            Log.d(TAG, "Signature length: " + signature.length());
            
            return signature;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating PayOS signature", e);
            throw new RuntimeException("Cannot create PayOS signature: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra trạng thái payment
     */
    public CompletableFuture<Payment> getPaymentStatus(String orderCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Payment payment = findPaymentByOrderCode(orderCode);
                if (payment == null) {
                    throw new RuntimeException("Payment not found");
                }
                
                String apiUrl = PayOSConfig.PAYOS_BASE_URL + 
                    PayOSConfig.GET_PAYMENT_INFO.replace("{orderCode}", orderCode);
                
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
                conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
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
                conn.disconnect();
                
                if (responseCode == 200) {
                    JSONObject responseJson = new JSONObject(responseBody);
                    
                    if ("00".equals(responseJson.optString("code", ""))) {
                        JSONObject data = responseJson.getJSONObject("data");
                        String status = data.optString("status", "PENDING");
                        
                        // Update payment status
                        switch (status.toLowerCase()) {
                            case "paid":
                            case "completed":
                                payment.setStatus(Payment.PaymentStatus.COMPLETED);
                                break;
                            case "cancelled":
                            case "canceled":
                                payment.setStatus(Payment.PaymentStatus.CANCELLED);
                                break;
                            case "failed":
                                payment.setStatus(Payment.PaymentStatus.FAILED);
                                break;
                            default:
                                payment.setStatus(Payment.PaymentStatus.PENDING);
                        }
                        
                        return payment;
                    }
                }
                
                return payment;
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking payment status", e);
                return findPaymentByOrderCode(orderCode);
            }
        });
    }

    /**
     * Xử lý payment thành công
     */
    public boolean processSuccessfulPayment(String orderCode, UUID userId) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            UserEntity user = databaseHelper.getUserById(userId.toString());
            
            if (user != null) {
                int currentQuota = user.getPostQuota() != null ? user.getPostQuota() : 0;
                int newQuota = currentQuota + PayOSConfig.DEFAULT_POST_QUOTA;
                user.setPostQuota(newQuota);
                
                boolean updated = databaseHelper.updateUser(user);
                
                if (updated) {
                    Log.d(TAG, "✅ User post quota updated: " + currentQuota + " -> " + newQuota);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error processing successful payment", e);
            return false;
        }
    }

    /**
     * Validate PayOS configuration
     */
    public boolean isPayOSConfigValid() {
        boolean isValid = PayOSConfig.CLIENT_ID != null && !PayOSConfig.CLIENT_ID.trim().isEmpty() &&
                         PayOSConfig.API_KEY != null && !PayOSConfig.API_KEY.trim().isEmpty() &&
                         PayOSConfig.CHECKSUM_KEY != null && !PayOSConfig.CHECKSUM_KEY.trim().isEmpty();
        
        Log.d(TAG, "PayOS Config validation: " + (isValid ? "✅ VALID" : "❌ INVALID"));
        return isValid;
    }

    /**
     * Test PayOS connectivity
     */
    public CompletableFuture<Boolean> testPayOSConnectivity() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(PayOSConfig.PAYOS_BASE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                int responseCode = conn.getResponseCode();
                conn.disconnect();
                
                boolean isConnected = responseCode > 0;
                Log.d(TAG, "PayOS connectivity test: " + (isConnected ? "✅ CONNECTED" : "❌ FAILED"));
                return isConnected;
                
            } catch (Exception e) {
                Log.e(TAG, "PayOS connectivity test failed", e);
                return false;
            }
        });
    }

    /**
     * Debug PayOS API response format
     */
    public CompletableFuture<String> debugPayOSResponse() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "=== DEBUGGING PAYOS API RESPONSE ===");
                
                // Test with minimal payload
                JSONObject testPayload = new JSONObject();
                testPayload.put("orderCode", System.currentTimeMillis() / 1000);
                testPayload.put("amount", 10000);
                testPayload.put("description", "Test Debug");
                
                JSONArray items = new JSONArray();
                JSONObject item = new JSONObject();
                item.put("name", "Debug Test");
                item.put("quantity", 1);
                item.put("price", 10000);
                items.put(item);
                testPayload.put("items", items);
                
                testPayload.put("returnUrl", PayOSConfig.SUCCESS_URL);
                testPayload.put("cancelUrl", PayOSConfig.CANCEL_URL);
                
                String signature = createPayOSSignature(testPayload);
                testPayload.put("signature", signature);
                
                URL url = new URL(PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
                conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                
                // Send request
                String jsonString = testPayload.toString();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                // Read response
                int responseCode = conn.getResponseCode();
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
                conn.disconnect();
                
                Log.d(TAG, "=== PAYOS DEBUG RESPONSE ===");
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response Body: " + responseBody);
                
                return "Debug Response: " + responseCode + " - " + responseBody;
                
            } catch (Exception e) {
                Log.e(TAG, "Debug PayOS API failed", e);
                return "Debug Error: " + e.getMessage();
            }
        });
    }

    /**
     * Force PayOS payment for debugging
     */
    public CompletableFuture<Payment> forcePayOSPayment(UUID userId, int amount, String description) {
        return createPaymentLink(userId, amount, description);
    }

    /**
     * Test PayOS Real API
     */
    public CompletableFuture<String> testPayOSRealAPI() {
        return debugPayOSResponse();
    }

    /**
     * Test PayOS với config thực để debug
     */
    public CompletableFuture<String> testRealPayOSConfig() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "=== TESTING REAL PAYOS CONFIG ===");
                Log.d(TAG, "CLIENT_ID: " + PayOSConfig.CLIENT_ID);
                Log.d(TAG, "API_KEY: " + PayOSConfig.API_KEY.substring(0, 8) + "...");
                Log.d(TAG, "CHECKSUM_KEY: " + PayOSConfig.CHECKSUM_KEY.substring(0, 16) + "...");
                Log.d(TAG, "BASE_URL: " + PayOSConfig.PAYOS_BASE_URL);
                
                // Test minimal payload
                JSONObject payload = new JSONObject();
                long orderCode = System.currentTimeMillis() / 1000;
                payload.put("orderCode", orderCode);
                payload.put("amount", 10000);
                payload.put("description", "Test config");
                
                JSONArray items = new JSONArray();
                JSONObject item = new JSONObject();
                item.put("name", "Test item");
                item.put("quantity", 1);
                item.put("price", 10000);
                items.put(item);
                payload.put("items", items);
                
                payload.put("returnUrl", PayOSConfig.SUCCESS_URL);
                payload.put("cancelUrl", PayOSConfig.CANCEL_URL);
                
                String signature = createPayOSSignature(payload);
                payload.put("signature", signature);
                
                Log.d(TAG, "Test signature: " + signature);
                Log.d(TAG, "Test payload: " + payload.toString());
                
                // Make API call
                URL url = new URL(PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
                conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                
                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                    os.flush();
                }
                
                // Read response
                int responseCode = conn.getResponseCode();
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
                conn.disconnect();
                
                Log.d(TAG, "=== REAL CONFIG TEST RESULT ===");
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response Body: " + responseBody);
                
                if (responseCode == 200 || responseCode == 201) {
                    try {
                        JSONObject responseJson = new JSONObject(responseBody);
                        String code = responseJson.optString("code", "");
                        
                        if ("00".equals(code)) {
                            JSONObject data = responseJson.getJSONObject("data");
                            String qrUrl = data.optString("qrCode", "");
                            String checkoutUrl = data.optString("checkoutUrl", "");
                            
                            return "✅ SUCCESS: PayOS config hoạt động!\nQR URL: " + qrUrl + "\nCheckout URL: " + checkoutUrl;
                        } else {
                            String desc = responseJson.optString("desc", "Unknown");
                            return "❌ PayOS Error Code " + code + ": " + desc;
                        }
                    } catch (JSONException e) {
                        return "❌ JSON Parse Error: " + e.getMessage() + "\nResponse: " + responseBody;
                    }
                } else {
                    return "❌ HTTP Error " + responseCode + ": " + responseBody;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Real config test failed", e);
                return "❌ Test Error: " + e.getMessage();
            }
        });
    }

    /**
     * Validate và log chi tiết về QR URL từ PayOS
     */
    private void validateAndLogQrUrl(String qrUrl) {
        Log.d(TAG, "=== VALIDATING PAYOS QR URL ===");
        Log.d(TAG, "QR URL: " + qrUrl);
        Log.d(TAG, "QR URL Length: " + qrUrl.length());
        Log.d(TAG, "QR URL Type Analysis:");
        
        if (qrUrl.startsWith("http://")) {
            Log.d(TAG, "  ✅ HTTP URL");
        } else if (qrUrl.startsWith("https://")) {
            Log.d(TAG, "  ✅ HTTPS URL");
        } else if (qrUrl.startsWith("data:image/")) {
            Log.d(TAG, "  ✅ Data URL (Base64 Image)");
            Log.d(TAG, "  Data URL format: " + qrUrl.substring(0, Math.min(50, qrUrl.length())) + "...");
        } else if (qrUrl.contains("payos.vn")) {
            Log.d(TAG, "  ✅ PayOS Domain URL");
        } else if (qrUrl.length() > 100 && qrUrl.matches("^[A-Za-z0-9+/]+=*$")) {
            Log.d(TAG, "  ✅ Possible Base64 String (no data prefix)");
        } else {
            Log.d(TAG, "  ⚠️ Unknown format");
            Log.d(TAG, "  First 100 chars: " + qrUrl.substring(0, Math.min(100, qrUrl.length())));
        }
        
        // Test if it's a valid URL
        if (qrUrl.startsWith("http")) {
            try {
                new java.net.URL(qrUrl);
                Log.d(TAG, "  ✅ Valid URL format");
            } catch (Exception e) {
                Log.e(TAG, "  ❌ Invalid URL format: " + e.getMessage());
            }
        }
        
        Log.d(TAG, "=== QR URL VALIDATION COMPLETE ===");
    }

    // Helper methods
    private Payment findPaymentByOrderCode(String orderCode) {
        return payments.stream()
                .filter(p -> orderCode.equals(p.getOrderCode()))
                .findFirst()
                .orElse(null);
    }

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

    /**
     * Simple test để xem PayOS trả về gì
     */
    public CompletableFuture<String> simplePayOSTest() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Log.d(TAG, "=== SIMPLE PAYOS TEST ===");
                
                // Minimal payload
                JSONObject payload = new JSONObject();
                long orderCode = System.currentTimeMillis() / 1000;
                payload.put("orderCode", orderCode);
                payload.put("amount", 10000);
                payload.put("description", "Test");
                
                JSONArray items = new JSONArray();
                JSONObject item = new JSONObject();
                item.put("name", "Test");
                item.put("quantity", 1);
                item.put("price", 10000);
                items.put(item);
                payload.put("items", items);
                
                payload.put("returnUrl", PayOSConfig.SUCCESS_URL);
                payload.put("cancelUrl", PayOSConfig.CANCEL_URL);
                
                String signature = createPayOSSignature(payload);
                payload.put("signature", signature);
                
                // Call API
                URL url = new URL(PayOSConfig.PAYOS_BASE_URL + PayOSConfig.CREATE_PAYMENT_LINK);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-client-id", PayOSConfig.CLIENT_ID);
                conn.setRequestProperty("x-api-key", PayOSConfig.API_KEY);
                conn.setDoOutput(true);
                
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }
                
                int responseCode = conn.getResponseCode();
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                String responseBody = response.toString();
                conn.disconnect();
                
                Log.d(TAG, "=== SIMPLE TEST RESULT ===");
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response Body: " + responseBody);
                
                if (responseCode == 200 || responseCode == 201) {
                    JSONObject responseJson = new JSONObject(responseBody);
                    if ("00".equals(responseJson.optString("code"))) {
                        JSONObject data = responseJson.getJSONObject("data");
                        
                        String qrCode = data.optString("qrCode", "EMPTY");
                        String checkoutUrl = data.optString("checkoutUrl", "EMPTY");
                        String paymentLinkId = data.optString("paymentLinkId", "EMPTY");
                        
                        String result = "SUCCESS:\n" +
                                "qrCode: " + qrCode.substring(0, Math.min(100, qrCode.length())) + "\n" +
                                "checkoutUrl: " + checkoutUrl.substring(0, Math.min(100, checkoutUrl.length())) + "\n" +
                                "paymentLinkId: " + paymentLinkId.substring(0, Math.min(100, paymentLinkId.length()));
                        
                        return result;
                    } else {
                        return "PayOS Error: " + responseJson.optString("desc", "Unknown");
                    }
                } else {
                    return "HTTP Error " + responseCode + ": " + responseBody;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Simple test failed", e);
                return "Exception: " + e.getMessage();
            }
        });
    }
}
