package com.example.projectprm392.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projectprm392.R;
import com.example.projectprm392.config.PayOSConfig;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.Payment;
import com.example.projectprm392.models.User;
import com.example.projectprm392.services.PayOSService;
import com.example.projectprm392.utils.PayOSAPITester;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    
    // Intent extras
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_DESCRIPTION = "description";
    
    // Views
    private Toolbar toolbar;
    private MaterialCardView cardPaymentInfo;
    private TextView tvAmount;
    private TextView tvDescription;
    private TextView tvPostQuota;
    private ImageView ivQrCode;
    private ProgressBar progressQrCode;
    private TextView tvQrStatus;
    private MaterialButton btnCheckStatus;
    private MaterialButton btnCancel;
    private ProgressBar progressCheckStatus;
    
    // Data
    private UUID userId;
    private int amount;
    private String description;
    private Payment currentPayment;
    private PayOSService payOSService;
    private DatabaseHelper databaseHelper;
    private Handler statusCheckHandler;
    private Runnable statusCheckRunnable;
    
    // Triple click detection for debug
    private long lastCancelClickTime = 0;
    private int cancelClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        // Initialize services
        payOSService = PayOSService.getInstance(this);
        databaseHelper = new DatabaseHelper(this);
        statusCheckHandler = new Handler(Looper.getMainLooper());
        
        // Get intent data
        getIntentData();
        
        // Initialize views
        initViews();
        setupToolbar();
        setupListeners();
        
        // Setup payment info
        setupPaymentInfo();
        
        // Debug: Test PayOS domain connectivity with cURL equivalent
        PayOSAPITester.testPayOSEndpoint();

        // Test domain resolution
        testPayOSDomainResolution();

        // Create payment
        createPayment();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String userIdString = intent.getStringExtra(EXTRA_USER_ID);
        if (userIdString != null && !userIdString.trim().isEmpty()) {
            try {
                userId = UUID.fromString(userIdString);
                Log.d(TAG, "✅ User ID từ Intent: " + userId);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "❌ Invalid UUID format from Intent: " + userIdString, e);
                userId = getCurrentUserIdFromSession();
            }
        } else {
            Log.w(TAG, "⚠️ No User ID in Intent, getting current user from session");
            userId = getCurrentUserIdFromSession();
        }
        
        // Ensure we have a valid user ID for PayOS
        if (userId == null) {
            Log.e(TAG, "❌ CRITICAL: Cannot get User ID for PayOS payment!");
            Toast.makeText(this, "❌ Lỗi: Không thể xác định User ID cho thanh toán", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        amount = intent.getIntExtra(EXTRA_AMOUNT, PayOSConfig.DEFAULT_AMOUNT);
        description = intent.getStringExtra(EXTRA_DESCRIPTION);
        if (description == null) {
            description = PayOSConfig.DEFAULT_DESCRIPTION;
        }
        
        Log.d(TAG, "=== INTENT DATA ===");
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Amount: " + amount);
        Log.d(TAG, "Description: " + description);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardPaymentInfo = findViewById(R.id.cardPaymentInfo);
        tvAmount = findViewById(R.id.tvAmount);
        tvDescription = findViewById(R.id.tvDescription);
        tvPostQuota = findViewById(R.id.tvPostQuota);
        ivQrCode = findViewById(R.id.ivQrCode);
        progressQrCode = findViewById(R.id.progressQrCode);
        tvQrStatus = findViewById(R.id.tvQrStatus);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnCancel = findViewById(R.id.btnCancel);
        progressCheckStatus = findViewById(R.id.progressCheckStatus);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán nạp tiền");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnCheckStatus.setOnClickListener(v -> checkPaymentStatus());
        btnCancel.setOnClickListener(v -> {
            stopStatusChecking();
            finish();
        });
        
        // Debug: Long press để retry payment (không có mock)
        cardPaymentInfo.setOnLongClickListener(v -> {
            Toast.makeText(this, "🔄 Thử tạo lại payment từ PayOS API...", Toast.LENGTH_SHORT).show();
            createPayment();
            return true;
        });
        
        // Double tap card to force REAL PayOS payment (no fallback)
        cardPaymentInfo.setOnClickListener(v -> {
            Toast.makeText(this, "🔥 Force Real PayOS API (no fallback)...", Toast.LENGTH_SHORT).show();
            forceRealPayOSPayment();
        });
        
        // Triple tap to test PayOS API response format
        ivQrCode.setOnLongClickListener(v -> {
            testSimplePayOS(); // Changed to simpler test
            return true;
        });
        
        // Quadruple tap QR to test advanced PayOS format
        ivQrCode.setOnClickListener(v -> {
            if (currentPayment != null && currentPayment.getQrCodeUrl() != null) {
                testQrUrlAccessibility();
            } else {
                // Advanced PayOS test
                testPayOSResponseFormat();
            }
        });
        
        // Test button để simulate thanh toán thành công
        btnCheckStatus.setOnLongClickListener(v -> {
            simulatePaymentSuccess();
            return true;
        });
        
        // Double tap to test PayOS connectivity
        btnCancel.setOnLongClickListener(v -> {
            testPayOSConnectivityDirectly();
            return true;
        });
        
        // Triple tap Cancel button to run comprehensive PayOS test
        btnCancel.setOnClickListener(v -> {
            if (System.currentTimeMillis() - lastCancelClickTime < 1000) {
                cancelClickCount++;
                if (cancelClickCount >= 3) {
                    comprehensivePayOSTest();
                    cancelClickCount = 0;
                    return;
                }
            } else {
                cancelClickCount = 1;
            }
            lastCancelClickTime = System.currentTimeMillis();
            
            // Normal cancel after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (cancelClickCount < 3) {
                    stopStatusChecking();
                    finish();
                }
            }, 1000);
        });
    }

    private void setupPaymentInfo() {
        tvAmount.setText(String.format("%,dđ", amount));
        
        // Hiển thị description đầy đủ cho user, nhưng PayOS sẽ nhận bản rút gọn
        String displayDescription = description;
        if (description.length() > 25) {
            displayDescription = description + "\n(PayOS sẽ nhận: " + description.substring(0, 25) + ")";
        }
        tvDescription.setText(displayDescription);
        
        tvPostQuota.setText("Giảm quota từ 20 xuống 10 (thêm 10 lượt đăng)");
        
        // Add instruction for testing
        Toast.makeText(this, 
            "💡 DEBUG TOOLS: Triple tap Cancel=comprehensive test | Long press QR=response debug | Tap QR=QR format test", 
            Toast.LENGTH_LONG).show();
    }

    private void createPayment() {
        showQrLoading(true);
        tvQrStatus.setText("🔗 Đang tạo PayOS payment (CHỈ REAL API)...");
        
        Log.d(TAG, "=== CREATING REAL PAYOS PAYMENT (NO MOCK FALLBACK) ===");
        
        // Direct call to create REAL PayOS payment ONLY - no mock fallback
        payOSService.createPaymentLink(userId, amount, description)
            .thenAccept(payment -> {
                runOnUiThread(() -> {
                    if (payment != null && payment.getQrCodeUrl() != null) {
                        currentPayment = payment;
                        
                        tvQrStatus.setText("✅ PayOS payment thành công! Đang tải QR từ API...");
                        Toast.makeText(this, "✅ Real PayOS payment created! Loading QR from API...", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "✅ Real PayOS QR URL từ API: " + payment.getQrCodeUrl());
                        
                        // Debug QR URL format
                        debugPayOSQrUrl(payment.getQrCodeUrl());
                        
                        loadQrCode(payment.getQrCodeUrl());
                        startAutoStatusCheck();
                    } else {
                        showQrLoading(false);
                        tvQrStatus.setText("❌ PayOS API trả về null payment");
                        showRetryOption();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    showQrLoading(false);
                    
                    String errorMsg = throwable.getMessage();
                    Log.e(TAG, "=== PAYOS API ERROR (NO MOCK FALLBACK) ===");
                    Log.e(TAG, "Error Type: " + throwable.getClass().getSimpleName());
                    Log.e(TAG, "Error Message: " + errorMsg);
                    Log.e(TAG, "Full Error:", throwable);
                    
                    String displayMessage;
                    if (errorMsg != null) {
                        if (errorMsg.contains("timeout") || errorMsg.contains("ConnectException")) {
                            displayMessage = "❌ Timeout kết nối PayOS. Kiểm tra internet.";
                        } else if (errorMsg.contains("Unable to resolve host") || errorMsg.contains("UnknownHostException") || errorMsg.contains("NXDOMAIN")) {
                            displayMessage = "❌ DNS Error: Không thể kết nối PayOS domain";
                        } else if (errorMsg.contains("PayOS Error Code 20")) {
                            displayMessage = "❌ Dữ liệu gửi PayOS không đúng định dạng";
                        } else if (errorMsg.contains("PayOS Error Code")) {
                            displayMessage = "❌ Lỗi PayOS API: " + errorMsg;
                        } else if (errorMsg.contains("HTTP Error 401")) {
                            displayMessage = "❌ PayOS credentials không đúng (401)";
                        } else if (errorMsg.contains("HTTP Error 403")) {
                            displayMessage = "❌ PayOS access bị từ chối (403)";
                        } else if (errorMsg.contains("HTTP Error 400")) {
                            displayMessage = "❌ PayOS request không hợp lệ (400)";
                        } else if (errorMsg.contains("HTTP Error")) {
                            displayMessage = "❌ Lỗi HTTP từ PayOS: " + errorMsg;
                        } else if (errorMsg.contains("Invalid PayOS response")) {
                            displayMessage = "❌ Phản hồi PayOS không hợp lệ";
                        } else if (errorMsg.contains("failed after") && errorMsg.contains("attempts")) {
                            displayMessage = "❌ PayOS không phản hồi sau nhiều lần thử";
                        } else {
                            displayMessage = "❌ PayOS API Error: " + errorMsg;
                        }
                    } else {
                        displayMessage = "❌ Lỗi không xác định từ PayOS API";
                    }
                    
                    tvQrStatus.setText(displayMessage);
                    Toast.makeText(this, displayMessage, Toast.LENGTH_LONG).show();
                    
                    // Show helpful debug options
                    Toast.makeText(this, "💡 Triple tap 'Hủy' để test PayOS credentials", Toast.LENGTH_LONG).show();
                    
                    // Show retry option - NO MOCK FALLBACK
                    showRetryOption();
                });
                return null;
            });
    }
    
    private void showRetryOption() {
        Toast.makeText(this, "Bấm 'Thử lại' để tạo lại PayOS payment", Toast.LENGTH_LONG).show();
        
        // Change button to retry
        btnCheckStatus.setText("Thử lại PayOS");
        btnCheckStatus.setEnabled(true);
        btnCheckStatus.setOnClickListener(v -> {
            btnCheckStatus.setText("Kiểm tra trạng thái");
            btnCheckStatus.setOnClickListener(v2 -> checkPaymentStatus());
            
            // Retry real PayOS payment
            Toast.makeText(this, "🔄 Đang thử lại PayOS API...", Toast.LENGTH_SHORT).show();
            createPayment();
        });
    }

    private void loadQrCode(String qrCodeUrl) {
        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            showQrLoading(false);
            tvQrStatus.setText("❌ PayOS API không trả về QR URL");
            Log.e(TAG, "QR Code URL is null or empty from PayOS API");
            Toast.makeText(this, "❌ Lỗi: PayOS API không cung cấp QR URL", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "=== LOADING QR CODE FROM PAYOS ===");
        Log.d(TAG, "QR URL: " + qrCodeUrl);
        Log.d(TAG, "QR URL Length: " + qrCodeUrl.length());
        Log.d(TAG, "QR URL starts with http: " + qrCodeUrl.startsWith("http"));
        Log.d(TAG, "QR URL starts with data: " + qrCodeUrl.startsWith("data:"));
        
        // Handle Base64 data URLs
        if (qrCodeUrl.startsWith("data:image/")) {
            Log.d(TAG, "🖼️ Processing Base64 QR image from PayOS");
            try {
                // Extract Base64 data
                String base64Data = qrCodeUrl.substring(qrCodeUrl.indexOf(",") + 1);
                byte[] decodedString = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                
                if (bitmap != null) {
                    ivQrCode.setImageBitmap(bitmap);
                    showQrLoading(false);
                    tvQrStatus.setText("✅ Quét mã QR để thanh toán PayOS");
                    Toast.makeText(this, "✅ QR PayOS đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Base64 QR Code displayed successfully");
                } else {
                    throw new Exception("Base64 decode failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to decode Base64 QR", e);
                showQrLoading(false);
                tvQrStatus.setText("❌ Lỗi decode QR Base64");
                ivQrCode.setImageResource(R.drawable.ic_cancel);
            }
            return;
        }
        
        // PayOS có thể trả về nhiều format URL khác nhau
        Log.d(TAG, "=== VALIDATING QR URL FORMAT ===");
        Log.d(TAG, "QR URL received: " + qrCodeUrl);
        Log.d(TAG, "QR URL length: " + qrCodeUrl.length());
        Log.d(TAG, "QR URL starts with http: " + qrCodeUrl.startsWith("http"));
        Log.d(TAG, "QR URL starts with data: " + qrCodeUrl.startsWith("data:"));
        
        // Chấp nhận nhiều format: HTTP URLs, data URLs, hoặc PayOS internal URLs
        boolean isValidFormat = qrCodeUrl.startsWith("http://") || 
                               qrCodeUrl.startsWith("https://") ||
                               qrCodeUrl.startsWith("data:image/") ||
                               qrCodeUrl.contains("payos.vn") ||
                               qrCodeUrl.length() > 50; // Có thể là Base64 string
        
        if (!isValidFormat) {
            Log.e(TAG, "Invalid QR URL format: " + qrCodeUrl);
            showQrLoading(false);
            tvQrStatus.setText("❌ PayOS trả về URL QR không hợp lệ: " + qrCodeUrl.substring(0, Math.min(50, qrCodeUrl.length())));
            Toast.makeText(this, "❌ URL QR từ PayOS không đúng định dạng: " + qrCodeUrl.substring(0, Math.min(100, qrCodeUrl.length())), Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "✅ QR URL format accepted: " + qrCodeUrl.substring(0, Math.min(50, qrCodeUrl.length())));
        
        // Load QR code - xử lý khác nhau theo format
        if (qrCodeUrl.startsWith("data:image/")) {
            // Base64 data URL - decode trực tiếp
            loadBase64QrCode(qrCodeUrl);
        } else {
            // HTTP URL - download image
            loadHttpQrCode(qrCodeUrl);
        }
    }
    
    private void loadBase64QrCode(String dataUrl) {
        try {
            Log.d(TAG, "=== LOADING BASE64 QR CODE ===");
            
            // Extract base64 data from data URL
            String base64Data = dataUrl.substring(dataUrl.indexOf(",") + 1);
            byte[] decodedBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
            
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            
            if (bitmap != null) {
                runOnUiThread(() -> {
                    ivQrCode.setImageBitmap(bitmap);
                    showQrLoading(false);
                    tvQrStatus.setText("✅ QR Code PayOS (Base64)");
                    Log.d(TAG, "✅ Base64 QR code loaded successfully");
                });
            } else {
                throw new Exception("Failed to decode Base64 image");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to load Base64 QR code", e);
            runOnUiThread(() -> {
                showQrLoading(false);
                tvQrStatus.setText("❌ Lỗi tải QR Base64");
                Toast.makeText(this, "❌ Không thể tải QR code Base64", Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void loadHttpQrCode(String qrCodeUrl) {
        Log.d(TAG, "=== STARTING QR LOAD FROM PAYOS ===");
        Log.d(TAG, "Original QR URL: " + qrCodeUrl);
        
        CompletableFuture.supplyAsync(() -> {
            HttpURLConnection connection = null;
            try {
                Log.d(TAG, "Step 1: Creating URL object from: " + qrCodeUrl);
                URL url = new URL(qrCodeUrl);
                
                Log.d(TAG, "Step 2: URL parsed successfully");
                Log.d(TAG, "  Protocol: " + url.getProtocol());
                Log.d(TAG, "  Host: " + url.getHost());
                Log.d(TAG, "  Port: " + url.getPort());
                Log.d(TAG, "  Path: " + url.getPath());
                Log.d(TAG, "  Query: " + url.getQuery());
                
                // Test DNS resolution first
                Log.d(TAG, "Step 3: Testing DNS resolution for: " + url.getHost());
                java.net.InetAddress addr = java.net.InetAddress.getByName(url.getHost());
                Log.d(TAG, "  DNS resolved to: " + addr.getHostAddress());
                
                Log.d(TAG, "Step 4: Opening HTTP connection to: " + url.getHost());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000); // Increased timeout
                connection.setReadTimeout(15000);
                connection.setRequestProperty("User-Agent", "Android-PayOS-QR-Loader/1.0");
                connection.setRequestProperty("Accept", "image/png,image/jpeg,image/gif,image/*,*/*");
                connection.setRequestProperty("Cache-Control", "no-cache, no-store");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setRequestProperty("Connection", "close");
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(true);
                
                Log.d(TAG, "Step 5: Attempting to connect to PayOS QR URL...");
                long startTime = System.currentTimeMillis();
                connection.connect();
                long connectTime = System.currentTimeMillis() - startTime;
                Log.d(TAG, "  Connection established in: " + connectTime + "ms");
                
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                String contentType = connection.getContentType();
                long contentLength = connection.getContentLengthLong();
                String finalUrl = connection.getURL().toString();
                
                Log.d(TAG, "=== PAYOS QR HTTP RESPONSE DETAILS ===");
                Log.d(TAG, "Response Code: " + responseCode);
                Log.d(TAG, "Response Message: " + responseMessage);
                Log.d(TAG, "Content Type: " + contentType);
                Log.d(TAG, "Content Length: " + contentLength);
                Log.d(TAG, "Final URL: " + finalUrl);
                Log.d(TAG, "Connection Time: " + connectTime + "ms");
                
                // Log all response headers
                Log.d(TAG, "=== RESPONSE HEADERS ===");
                java.util.Map<String, java.util.List<String>> headers = connection.getHeaderFields();
                for (java.util.Map.Entry<String, java.util.List<String>> entry : headers.entrySet()) {
                    String key = entry.getKey();
                    java.util.List<String> values = entry.getValue();
                    Log.d(TAG, (key != null ? key : "Status") + ": " + values);
                }
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Step 6: HTTP 200 - Checking content type");
                    
                    // Check content type
                    if (contentType != null && !contentType.toLowerCase().contains("image")) {
                        Log.e(TAG, "❌ PayOS QR URL returned non-image content: " + contentType);
                        Log.e(TAG, "Response body might be error message, checking...");
                        
                        // Read response body to see error message
                        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                                new java.io.InputStreamReader(connection.getInputStream()))) {
                            StringBuilder responseBody = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                responseBody.append(line).append("\n");
                            }
                            Log.e(TAG, "PayOS QR URL response body: " + responseBody.toString());
                        }
                        return null;
                    }
                    
                    Log.d(TAG, "Step 7: Content type OK, reading image stream");
                    InputStream input = connection.getInputStream();
                    Log.d(TAG, "Step 8: Input stream obtained, decoding bitmap...");
                    
                    long decodeStartTime = System.currentTimeMillis();
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    long decodeTime = System.currentTimeMillis() - decodeStartTime;
                    input.close();
                    
                    if (bitmap != null) {
                        Log.d(TAG, "✅ PayOS QR Image successfully loaded:");
                        Log.d(TAG, "  Dimensions: " + bitmap.getWidth() + "x" + bitmap.getHeight() + " pixels");
                        Log.d(TAG, "  Config: " + bitmap.getConfig());
                        Log.d(TAG, "  Byte count: " + bitmap.getByteCount());
                        Log.d(TAG, "  Decode time: " + decodeTime + "ms");
                        return bitmap;
                    } else {
                        Log.e(TAG, "❌ PayOS QR data corrupted or not a valid image");
                        Log.e(TAG, "BitmapFactory.decodeStream returned null");
                        Log.e(TAG, "Content-Type was: " + contentType);
                        Log.e(TAG, "Content-Length was: " + contentLength);
                        return null;
                    }
                } else {
                    Log.e(TAG, "❌ PayOS QR URL returned error: " + responseCode + " " + responseMessage);
                    
                    // Read error response body
                    try {
                        java.io.InputStream errorStream = connection.getErrorStream();
                        if (errorStream != null) {
                            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(errorStream))) {
                                StringBuilder errorBody = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    errorBody.append(line).append("\n");
                                }
                                Log.e(TAG, "PayOS QR Error response body: " + errorBody.toString());
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read error response", e);
                    }
                    
                    return null;
                }
            } catch (java.net.MalformedURLException e) {
                Log.e(TAG, "❌ PayOS QR URL format invalid: " + qrCodeUrl, e);
                Log.e(TAG, "  Error details: " + e.getMessage());
                return null;
            } catch (java.net.UnknownHostException e) {
                Log.e(TAG, "❌ Cannot resolve PayOS QR domain: " + extractDomain(qrCodeUrl), e);
                Log.e(TAG, "  DNS Error: " + e.getMessage());
                Log.e(TAG, "  This might be a network/DNS issue");
                return null;
            } catch (java.net.ConnectException e) {
                Log.e(TAG, "❌ Connection refused by PayOS QR server: " + extractDomain(qrCodeUrl), e);
                Log.e(TAG, "  Connect Error: " + e.getMessage());
                Log.e(TAG, "  Server might be down or blocking connections");
                return null;
            } catch (java.net.SocketTimeoutException e) {
                Log.e(TAG, "❌ Timeout loading PayOS QR from: " + extractDomain(qrCodeUrl), e);
                Log.e(TAG, "  Timeout Error: " + e.getMessage());
                Log.e(TAG, "  Server is slow or unresponsive");
                return null;
            } catch (javax.net.ssl.SSLException e) {
                Log.e(TAG, "❌ SSL error loading PayOS QR from: " + extractDomain(qrCodeUrl), e);
                Log.e(TAG, "  SSL Error: " + e.getMessage());
                Log.e(TAG, "  Certificate or SSL configuration issue");
                return null;
            } catch (java.io.IOException e) {
                Log.e(TAG, "❌ IO error loading PayOS QR", e);
                Log.e(TAG, "  IO Error: " + e.getMessage());
                Log.e(TAG, "  Network or stream read issue");
                return null;
            } catch (Exception e) {
                Log.e(TAG, "❌ Unexpected error loading PayOS QR", e);
                Log.e(TAG, "QR URL was: " + qrCodeUrl);
                Log.e(TAG, "Error type: " + e.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + e.getMessage());
                Log.e(TAG, "Stack trace:", e);
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).thenAccept(bitmap -> {
            runOnUiThread(() -> {
                if (bitmap != null) {
                    Log.d(TAG, "=== QR BITMAP DISPLAY SUCCESS ===");
                    ivQrCode.setImageBitmap(bitmap);
                    showQrLoading(false);
                    tvQrStatus.setText("✅ Quét mã QR để thanh toán PayOS");
                    
                    Toast.makeText(this, "✅ QR PayOS đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ PayOS QR Code displayed successfully");
                } else {
                    Log.e(TAG, "=== QR BITMAP DISPLAY FAILED ===");
                    showQrLoading(false);
                    tvQrStatus.setText("❌ Không thể tải QR từ PayOS server");
                    
                    Log.e(TAG, "❌ Failed to load PayOS QR - NO FALLBACK");
                    Toast.makeText(this, "❌ PayOS QR URL không khả dụng. Long press QR để test chi tiết.", Toast.LENGTH_LONG).show();
                    
                    // NO FALLBACK QR - Just show error icon
                    ivQrCode.setImageResource(R.drawable.ic_cancel); // Assuming you have an error icon
                }
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                Log.e(TAG, "=== QR LOADING EXCEPTION ===");
                showQrLoading(false);
                tvQrStatus.setText("❌ Lỗi tải QR từ PayOS");
                
                Log.e(TAG, "❌ PayOS QR loading exception - NO FALLBACK", throwable);
                String errorMsg = throwable.getMessage();
                String detailedError = "Lỗi: " + throwable.getClass().getSimpleName();
                if (errorMsg != null) {
                    detailedError += " - " + errorMsg;
                }
                
                Toast.makeText(this, "❌ " + detailedError + ". Long press QR để debug.", Toast.LENGTH_LONG).show();
                
                // NO FALLBACK QR - Just show error icon
                ivQrCode.setImageResource(R.drawable.ic_cancel); // Assuming you have an error icon
            });
            return null;
        });
    }

    private void showQrLoading(boolean show) {
        progressQrCode.setVisibility(show ? View.VISIBLE : View.GONE);
        ivQrCode.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void checkPaymentStatus() {
        if (currentPayment == null) {
            Toast.makeText(this, "Chưa có thông tin thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        showStatusCheckLoading(true);
        
        payOSService.getPaymentStatus(currentPayment.getOrderCode())
            .thenAccept(payment -> {
                runOnUiThread(() -> {
                    showStatusCheckLoading(false);
                    handlePaymentStatusUpdate(payment);
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    showStatusCheckLoading(false);
                    Toast.makeText(this, "Lỗi kiểm tra trạng thái: " + throwable.getMessage(), 
                        Toast.LENGTH_LONG).show();
                });
                return null;
            });
    }

    private void handlePaymentStatusUpdate(Payment payment) {
        currentPayment = payment;
        
        if (payment.isSuccessful()) {
            // Payment thành công
            stopStatusChecking();
            
            // Process payment
            boolean processed = payOSService.processSuccessfulPayment(
                payment.getOrderCode(), userId);
            
            if (processed) {
                // Chuyển đến success activity
                Intent successIntent = new Intent(this, PaymentSuccessActivity.class);
                successIntent.putExtra(PaymentSuccessActivity.EXTRA_PAYMENT_ID, 
                    payment.getPaymentId().toString());
                successIntent.putExtra(PaymentSuccessActivity.EXTRA_AMOUNT, amount);
                successIntent.putExtra(PaymentSuccessActivity.EXTRA_POST_QUOTA, 
                    PayOSConfig.DEFAULT_POST_QUOTA);
                startActivity(successIntent);
                finish();
            } else {
                Toast.makeText(this, "Lỗi xử lý thanh toán thành công", Toast.LENGTH_LONG).show();
            }
            
        } else if (payment.isFailed()) {
            // Payment thất bại
            stopStatusChecking();
            tvQrStatus.setText("❌ Thanh toán thất bại hoặc đã bị hủy");
            Toast.makeText(this, "Thanh toán không thành công", Toast.LENGTH_SHORT).show();
            
        } else {
            // Vẫn đang pending
            tvQrStatus.setText("⏳ Đang chờ thanh toán...");
        }
    }

    private void startAutoStatusCheck() {
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentPayment != null && currentPayment.isPending()) {
                    checkPaymentStatus();
                    // Check again after 5 seconds
                    statusCheckHandler.postDelayed(this, 5000);
                }
            }
        };
        
        // Start checking after 10 seconds
        statusCheckHandler.postDelayed(statusCheckRunnable, 10000);
    }

    private void stopStatusChecking() {
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
        }
    }

    private void showStatusCheckLoading(boolean show) {
        progressCheckStatus.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCheckStatus.setEnabled(!show);
    }

    // Method để test simulate payment success
    private void simulatePaymentSuccess() {
        if (currentPayment != null) {
            Toast.makeText(this, "Simulating payment success for testing...", Toast.LENGTH_SHORT).show();
            currentPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            handlePaymentStatusUpdate(currentPayment);
        } else {
            Toast.makeText(this, "❌ Không có payment để simulate - Tạo PayOS payment trước", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Test PayOS domain resolution for debugging
     */
    private void testPayOSDomainResolution() {
        new Thread(() -> {
            try {
                Log.d(TAG, "=== TESTING DOMAIN RESOLUTION ===");
                
                // Test correct domain
                java.net.InetAddress correctAddr = java.net.InetAddress.getByName("api-merchant.payos.vn");
                Log.d(TAG, "✅ api-merchant.payos.vn resolves to: " + correctAddr.getHostAddress());
                
                // Test wrong domain
                try {
                    java.net.InetAddress wrongAddr = java.net.InetAddress.getByName("api.payos.vn");
                    Log.d(TAG, "❓ api.payos.vn resolves to: " + wrongAddr.getHostAddress());
                } catch (java.net.UnknownHostException e) {
                    Log.d(TAG, "❌ api.payos.vn KHÔNG tồn tại (đúng như mong đợi): " + e.getMessage());
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Domain resolution test failed", e);
            }
        }).start();
    }
    
    /**
     * Test PayOS connectivity directly with detailed logging
     */
    private void testPayOSConnectivityDirectly() {
        Log.d(TAG, "=== TEST PAYOS CONNECTIVITY ===");
        
        Toast.makeText(this, "Testing PayOS connectivity...", Toast.LENGTH_SHORT).show();
        
        payOSService.testPayOSConnectivity()
            .whenComplete((isConnected, exception) -> {
                runOnUiThread(() -> {
                    String message;
                    if (exception != null) {
                        message = "❌ Connectivity test failed: " + exception.getMessage();
                    } else if (isConnected) {
                        message = "✅ PayOS domain is reachable!";
                    } else {
                        message = "❌ PayOS domain is unreachable";
                    }
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    Log.d(TAG, message);
                });
            });
    }

    /**
     * Test PayOS API response to debug QR URL format
     */
    private void testPayOSResponseFormat() {
        Toast.makeText(this, "🔍 Testing PayOS real config...", Toast.LENGTH_SHORT).show();
        
        payOSService.testRealPayOSConfig()
            .thenAccept(result -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "PayOS Real Config Test Result: " + result);
                    
                    // Show detailed result in Toast
                    if (result.contains("SUCCESS")) {
                        Toast.makeText(this, "✅ PayOS Config OK! " + result.substring(0, Math.min(100, result.length())), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "❌ PayOS Error: " + result.substring(0, Math.min(100, result.length())), Toast.LENGTH_LONG).show();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ PayOS Test error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
    }
    
    /**
     * Extract domain from URL for debugging
     */
    private String extractDomain(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            return "unknown";
        }
    }
    


    /**
     * Debug method to analyze PayOS QR URL format and accessibility
     */
    private void debugPayOSQrUrl(String qrUrl) {
        if (qrUrl == null) {
            Log.w(TAG, "=== QR URL DEBUG: NULL URL ===");
            return;
        }
        
        Log.d(TAG, "=== PAYOS QR URL DEBUG ===");
        Log.d(TAG, "Full URL: " + qrUrl);
        Log.d(TAG, "URL Length: " + qrUrl.length());
        Log.d(TAG, "Domain: " + extractDomain(qrUrl));
        Log.d(TAG, "Protocol: " + (qrUrl.startsWith("https://") ? "HTTPS" : qrUrl.startsWith("http://") ? "HTTP" : "UNKNOWN"));
        Log.d(TAG, "Contains 'qr': " + qrUrl.toLowerCase().contains("qr"));
        Log.d(TAG, "Contains 'png': " + qrUrl.toLowerCase().contains("png"));
        Log.d(TAG, "Contains 'jpg': " + qrUrl.toLowerCase().contains("jpg"));
        Log.d(TAG, "Contains 'jpeg': " + qrUrl.toLowerCase().contains("jpeg"));
        
        // Check if URL looks like image URL
        String lowerUrl = qrUrl.toLowerCase();
        boolean looksLikeImage = lowerUrl.endsWith(".png") || lowerUrl.endsWith(".jpg") || 
                                lowerUrl.endsWith(".jpeg") || lowerUrl.endsWith(".gif") ||
                                lowerUrl.contains("/qr") || lowerUrl.contains("image");
        Log.d(TAG, "Looks like image URL: " + looksLikeImage);
        
        // Test domain resolution for QR URL
        new Thread(() -> {
            try {
                String domain = extractDomain(qrUrl);
                java.net.InetAddress addr = java.net.InetAddress.getByName(domain);
                Log.d(TAG, "QR Domain '" + domain + "' resolves to: " + addr.getHostAddress());
            } catch (Exception e) {
                Log.e(TAG, "QR Domain resolution failed: " + extractDomain(qrUrl), e);
            }
        }).start();
    }

    /**
     * Quick test method for QR URL accessibility
     */
    private void testQrUrlAccessibility() {
        if (currentPayment == null || currentPayment.getQrCodeUrl() == null) {
            Toast.makeText(this, "❌ Không có QR URL để test", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String qrUrl = currentPayment.getQrCodeUrl();
        Toast.makeText(this, "🔍 Testing QR URL: " + extractDomain(qrUrl), Toast.LENGTH_SHORT).show();
        
        new Thread(() -> {
            try {
                URL url = new URL(qrUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("HEAD"); // Just check headers, don't download
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "Android-QR-Test/1.0");
                
                int responseCode = conn.getResponseCode();
                String contentType = conn.getContentType();
                long contentLength = conn.getContentLengthLong();
                
                conn.disconnect();
                
                runOnUiThread(() -> {
                    String result = "QR Test: " + responseCode + 
                                  "\nType: " + contentType + 
                                  "\nSize: " + contentLength + " bytes";
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                    
                    Log.d(TAG, "=== QR URL TEST RESULT ===");
                    Log.d(TAG, "Response Code: " + responseCode);
                    Log.d(TAG, "Content Type: " + contentType);
                    Log.d(TAG, "Content Length: " + contentLength);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ QR URL Test Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, "QR URL test failed", e);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusChecking();
    }
    
    /**
     * Force create real PayOS payment without fallback to mock
     * Use this to debug exact PayOS API errors
     */
    private void forceRealPayOSPayment() {
        Log.d(TAG, "=== FORCE REAL PAYOS PAYMENT TEST ===");
        
        showPaymentLoading();
        tvQrStatus.setText("🔥 Testing REAL PayOS API (no fallback)...");
        
        payOSService.forcePayOSPayment(userId, amount, description)
            .whenComplete((payment, exception) -> {
                runOnUiThread(() -> {
                    if (exception != null) {
                        Log.e(TAG, "❌ Force PayOS FAILED", exception);
                        hidePaymentLoading();
                        tvQrStatus.setText("❌ PayOS API FAILED: " + exception.getMessage());
                        
                        // Show detailed error dialog
                        new AlertDialog.Builder(this)
                            .setTitle("PayOS API Error")
                            .setMessage("Force PayOS API failed:\n\n" + 
                                       "Error: " + exception.getClass().getSimpleName() + "\n" +
                                       "Message: " + exception.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                    } else {
                        Log.d(TAG, "✅ Force PayOS SUCCESS!");
                        currentPayment = payment;
                        displayPaymentSuccess(payment);
                        tvQrStatus.setText("✅ REAL PayOS API SUCCESS!");
                    }
                });
            });
    }

    /**
     * Test comprehensive PayOS API functions
     */
    private void comprehensivePayOSTest() {
        Log.d(TAG, "=== COMPREHENSIVE PAYOS TEST ===");
        
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("Testing PayOS API")
            .setMessage("Running comprehensive PayOS tests...")
            .setCancelable(false)
            .create();
        progressDialog.show();
        
        StringBuilder testResults = new StringBuilder();
        testResults.append("PayOS API Test Results:\n\n");
        
        // Test 1: PayOS connectivity
        payOSService.testPayOSConnectivity()
            .thenCompose(connectivity -> {
                testResults.append("1. Connectivity: ").append(connectivity ? "✅ PASS" : "❌ FAIL").append("\n");
                
                // Test 2: PayOS API response format
                return payOSService.testPayOSRealAPI();
            })
            .thenCompose(apiResult -> {
                testResults.append("2. API Test: ").append(apiResult).append("\n");
                
                // Test 3: Configuration validation
                testResults.append("3. Config Check: ");
                try {
                    boolean configValid = payOSService.isPayOSConfigValid();
                    testResults.append(configValid ? "✅ VALID" : "❌ INVALID").append("\n");
                } catch (Exception e) {
                    testResults.append("❌ ERROR: ").append(e.getMessage()).append("\n");
                }
                
                return CompletableFuture.completedFuture("Tests completed");
            })
            .whenComplete((result, exception) -> {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    if (exception != null) {
                        testResults.append("\n❌ Test Exception: ").append(exception.getMessage());
                    }
                    
                    new AlertDialog.Builder(this)
                        .setTitle("PayOS Comprehensive Test")
                        .setMessage(testResults.toString())
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Copy", (dialog, which) -> {
                            // Copy to clipboard
                            android.content.ClipboardManager clipboard = 
                                (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                            android.content.ClipData clip = android.content.ClipData.newPlainText("PayOS Test", testResults.toString());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(this, "Test results copied!", Toast.LENGTH_SHORT).show();
                        })
                        .show();
                });
            });
    }
    
    /**
     * Display payment success and load QR code
     */
    private void displayPaymentSuccess(Payment payment) {
        if (payment == null) {
            Log.e(TAG, "❌ Cannot display null payment");
            tvQrStatus.setText("❌ Payment object is null");
            return;
        }
        
        currentPayment = payment;
        String qrUrl = payment.getQrCodeUrl();
        
        Log.d(TAG, "=== DISPLAYING REAL PAYOS PAYMENT ===");
        Log.d(TAG, "Order Code: " + payment.getOrderCode());
        Log.d(TAG, "QR URL: " + qrUrl);
        
        // Always treat as REAL PayOS payment since we removed mock
        Log.d(TAG, "✅ REAL PayOS payment detected");
        tvQrStatus.setText("✅ PayOS thành công! QR code thật từ PayOS API");
        Toast.makeText(this, "✅ Real PayOS QR code từ API!", Toast.LENGTH_SHORT).show();
        
        // Load QR code from PayOS
        if (qrUrl != null && !qrUrl.trim().isEmpty()) {
            loadQrCode(qrUrl);
            startAutoStatusCheck();
        } else {
            Log.e(TAG, "❌ QR URL is empty from PayOS");
            tvQrStatus.setText("❌ PayOS không trả về QR URL");
            showRetryOption();
        }
    }
    
    /**
     * Show/hide payment loading state
     */
    private void showPaymentLoading() {
        showQrLoading(true);
        btnCheckStatus.setEnabled(false);
        btnCancel.setEnabled(false);
    }
    
    private void hidePaymentLoading() {
        showQrLoading(false);
        btnCheckStatus.setEnabled(true);
        btnCancel.setEnabled(true);
    }
    
    /**
     * Get current user ID from session when Intent doesn't provide valid User ID
     */
    private UUID getCurrentUserIdFromSession() {
        try {
            // Try to get current user from LoginController
            LoginController loginController = new LoginController(this);
            User currentUser = loginController.getCurrentUser();
            
            if (currentUser != null && currentUser.getUserId() != null) {
                Log.d(TAG, "✅ Got User ID from session: " + currentUser.getUserId());
                return currentUser.getUserId();
            } else {
                Log.w(TAG, "⚠️ No current user in session, creating default user");
                return createDefaultUser();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting current user from session", e);
            return createDefaultUser();
        }
    }
    
    /**
     * Create a default user for payment when no user is logged in
     */
    private UUID createDefaultUser() {
        try {
            Log.d(TAG, "🔄 Creating default user for PayOS payment...");
            
            // Create a default user for payment purposes
            User defaultUser = new User();
            defaultUser.setEmail("guest@payos.payment");
            defaultUser.setFullName("Guest User");
            defaultUser.setRole("USER");
            defaultUser.setPostQuota(0);
            
            // Insert into database
            long insertResult = databaseHelper.insertUser(defaultUser);
            
            if (insertResult > 0) {
                Log.d(TAG, "✅ Default user created successfully: " + defaultUser.getUserId());
                Toast.makeText(this, "ℹ️ Đã tạo user mặc định cho thanh toán", Toast.LENGTH_SHORT).show();
                return defaultUser.getUserId();
            } else {
                Log.e(TAG, "❌ Failed to insert default user to database");
                // Return generated UUID anyway for PayOS payment
                return defaultUser.getUserId();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating default user", e);
            // As last resort, generate a random UUID for PayOS
            UUID fallbackUserId = UUID.randomUUID();
            Log.w(TAG, "⚠️ Using fallback UUID for PayOS: " + fallbackUserId);
            return fallbackUserId;
        }
    }

    /**
     * Test PayOS simple để xem response format
     */
    private void testSimplePayOS() {
        Toast.makeText(this, "🔍 Testing simple PayOS...", Toast.LENGTH_SHORT).show();
        
        payOSService.simplePayOSTest()
            .thenAccept(result -> {
                runOnUiThread(() -> {
                    Log.d(TAG, "Simple PayOS Test Result: " + result);
                    
                    // Show in dialog for easier reading
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("PayOS Simple Test Result")
                        .setMessage(result)
                        .setPositiveButton("OK", null)
                        .setNeutralButton("Test QR URL", (dialog, which) -> {
                            // Extract QR URL from result and test it
                            testQrUrlFromResult(result);
                        })
                        .show();
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Simple PayOS test error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                });
                return null;
            });
    }

    /**
     * Test QR URL từ PayOS response
     */
    private void testQrUrlFromResult(String result) {
        try {
            // Extract QR URL from result
            String qrUrl = null;
            if (result.contains("qrCode:")) {
                String[] lines = result.split("\n");
                for (String line : lines) {
                    if (line.contains("qrCode:")) {
                        qrUrl = line.substring(line.indexOf(":") + 1).trim();
                        break;
                    }
                }
            }
            
            if (qrUrl != null && !qrUrl.equals("EMPTY")) {
                Log.d(TAG, "=== TESTING EXTRACTED QR URL ===");
                Log.d(TAG, "QR URL: " + qrUrl);
                
                // Test this URL directly
                testSpecificQrUrl(qrUrl);
            } else {
                Toast.makeText(this, "❌ Không tìm thấy QR URL trong response", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting QR URL", e);
            Toast.makeText(this, "❌ Lỗi extract QR URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Test một QR URL cụ thể
     */
    private void testSpecificQrUrl(String qrUrl) {
        AlertDialog progressDialog = new AlertDialog.Builder(this)
            .setTitle("Testing QR URL")
            .setMessage("Testing: " + qrUrl.substring(0, Math.min(50, qrUrl.length())) + "...")
            .setCancelable(false)
            .create();
        progressDialog.show();
        
        CompletableFuture.supplyAsync(() -> {
            StringBuilder testResult = new StringBuilder();
            testResult.append("=== QR URL TEST ===\n");
            testResult.append("URL: ").append(qrUrl).append("\n\n");
            
            HttpURLConnection connection = null;
            try {
                // Test 1: URL parsing
                URL url = new URL(qrUrl);
                testResult.append("✅ URL Parse: OK\n");
                testResult.append("Protocol: ").append(url.getProtocol()).append("\n");
                testResult.append("Host: ").append(url.getHost()).append("\n");
                testResult.append("Path: ").append(url.getPath()).append("\n\n");
                
                // Test 2: DNS resolution
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName(url.getHost());
                    testResult.append("✅ DNS: ").append(addr.getHostAddress()).append("\n\n");
                } catch (Exception e) {
                    testResult.append("❌ DNS: ").append(e.getMessage()).append("\n\n");
                    return testResult.toString();
                }
                
                // Test 3: HTTP connection
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("User-Agent", "Android-PayOS-QR-Test/1.0");
                connection.setRequestProperty("Accept", "image/*,*/*");
                connection.setInstanceFollowRedirects(true);
                
                // Get response
                int responseCode = connection.getResponseCode();
                String responseMessage = connection.getResponseMessage();
                String contentType = connection.getContentType();
                long contentLength = connection.getContentLengthLong();
                
                testResult.append("=== HTTP RESPONSE ===\n");
                testResult.append("Status: ").append(responseCode).append(" ").append(responseMessage).append("\n");
                testResult.append("Content-Type: ").append(contentType).append("\n");
                testResult.append("Content-Length: ").append(contentLength).append("\n");
                testResult.append("Final URL: ").append(connection.getURL().toString()).append("\n\n");
                
                if (responseCode == 200) {
                    testResult.append("✅ QR URL accessible!\n");
                    
                    // Test actual image download
                    if (contentType != null && contentType.toLowerCase().contains("image")) {
                        testResult.append("✅ Content is image\n");
                        
                        // Try to download and decode
                        connection.disconnect();
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);
                        connection.setRequestProperty("User-Agent", "Android-PayOS-QR-Test/1.0");
                        connection.setRequestProperty("Accept", "image/*,*/*");
                        
                        try (InputStream input = connection.getInputStream()) {
                            Bitmap bitmap = BitmapFactory.decodeStream(input);
                            if (bitmap != null) {
                                testResult.append("✅ Image decode: ").append(bitmap.getWidth()).append("x").append(bitmap.getHeight()).append("\n");
                            } else {
                                testResult.append("❌ Image decode failed\n");
                            }
                        }
                    } else {
                        testResult.append("❌ Content is not image: ").append(contentType).append("\n");
                    }
                } else {
                    testResult.append("❌ HTTP Error: ").append(responseCode).append("\n");
                }
                
            } catch (java.net.MalformedURLException e) {
                testResult.append("❌ URL Format: ").append(e.getMessage()).append("\n");
            } catch (java.net.UnknownHostException e) {
                testResult.append("❌ DNS Lookup: ").append(e.getMessage()).append("\n");
            } catch (java.net.ConnectException e) {
                testResult.append("❌ Connection: ").append(e.getMessage()).append("\n");
            } catch (java.net.SocketTimeoutException e) {
                testResult.append("❌ Timeout: ").append(e.getMessage()).append("\n");
            } catch (javax.net.ssl.SSLException e) {
                testResult.append("❌ SSL Error: ").append(e.getMessage()).append("\n");
            } catch (Exception e) {
                testResult.append("❌ Error: ").append(e.getClass().getSimpleName()).append(" - ").append(e.getMessage()).append("\n");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            
            return testResult.toString();
            
        }).thenAccept(result -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                
                // Show detailed result
                new AlertDialog.Builder(this)
                    .setTitle("QR URL Test Result")
                    .setMessage(result)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("Copy", (dialog, which) -> {
                        android.content.ClipboardManager clipboard = 
                            (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("QR Test", result);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Result copied!", Toast.LENGTH_SHORT).show();
                    })
                    .show();
                
                Log.d(TAG, "QR URL Test Result:\n" + result);
            });
        }).exceptionally(throwable -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "❌ QR Test Error: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            });
            return null;
        });
    }
}
