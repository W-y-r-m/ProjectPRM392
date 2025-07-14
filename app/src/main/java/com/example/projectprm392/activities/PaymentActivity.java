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

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projectprm392.R;
import com.example.projectprm392.config.PayOSConfig;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.Payment;
import com.example.projectprm392.services.PayOSService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
        
        // Create payment
        createPayment();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        String userIdString = intent.getStringExtra(EXTRA_USER_ID);
        if (userIdString != null) {
            userId = UUID.fromString(userIdString);
        }
        amount = intent.getIntExtra(EXTRA_AMOUNT, PayOSConfig.DEFAULT_AMOUNT);
        description = intent.getStringExtra(EXTRA_DESCRIPTION);
        if (description == null) {
            description = PayOSConfig.DEFAULT_DESCRIPTION;
        }
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
        
        // Debug: Long press để tạo mock payment
        cardPaymentInfo.setOnLongClickListener(v -> {
            createMockPaymentForTesting();
            return true;
        });
        
        // Test button để simulate thanh toán thành công
        btnCheckStatus.setOnLongClickListener(v -> {
            simulatePaymentSuccess();
            return true;
        });
    }

    private void setupPaymentInfo() {
        tvAmount.setText(String.format("%,dđ", amount));
        tvDescription.setText(description);
        tvPostQuota.setText("Giảm quota từ 20 xuống 10 (thêm 10 lượt đăng)");
        
        // Add instruction for testing
        Toast.makeText(this, 
            "💡 Long press 'Kiểm tra trạng thái' để test thanh toán thành công", 
            Toast.LENGTH_LONG).show();
    }

    private void createPayment() {
        showQrLoading(true);
        tvQrStatus.setText("🔗 Đang kết nối PayOS API...");
        
        payOSService.createPaymentLink(userId, amount, description)
            .thenAccept(payment -> {
                runOnUiThread(() -> {
                    if (payment != null && payment.getQrCodeUrl() != null) {
                        currentPayment = payment;
                        tvQrStatus.setText("✅ Đã tạo PayOS QR thành công!");
                        loadQrCode(payment.getQrCodeUrl());
                        startAutoStatusCheck();
                        
                        Toast.makeText(this, "✅ PayOS QR đã được tạo thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        showQrLoading(false);
                        tvQrStatus.setText("❌ PayOS API không trả về QR code");
                        showRetryOption();
                    }
                });
            })
            .exceptionally(throwable -> {
                runOnUiThread(() -> {
                    showQrLoading(false);
                    
                    String errorMsg = throwable.getMessage();
                    Log.e(TAG, "PayOS API Error", throwable);
                    
                    if (errorMsg != null) {
                        if (errorMsg.contains("timeout") || errorMsg.contains("ConnectException")) {
                            tvQrStatus.setText("❌ Timeout kết nối PayOS. Kiểm tra internet.");
                        } else if (errorMsg.contains("PayOS API Error")) {
                            tvQrStatus.setText("❌ Lỗi PayOS API. Kiểm tra cấu hình.");
                        } else if (errorMsg.contains("HTTP Error")) {
                            tvQrStatus.setText("❌ Lỗi HTTP từ PayOS server.");
                        } else {
                            tvQrStatus.setText("❌ Lỗi PayOS: " + errorMsg);
                        }
                    } else {
                        tvQrStatus.setText("❌ Lỗi không xác định từ PayOS API");
                    }
                    
                    Toast.makeText(this, "❌ PayOS Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    showRetryOption();
                });
                return null;
            });
    }
    
    private void showRetryOption() {
        Toast.makeText(this, "Bấm 'Thử lại' để tạo lại thanh toán", Toast.LENGTH_LONG).show();
        
        // Change button to retry
        btnCheckStatus.setText("Thử lại");
        btnCheckStatus.setEnabled(true);
        btnCheckStatus.setOnClickListener(v -> {
            btnCheckStatus.setText("Kiểm tra trạng thái");
            btnCheckStatus.setOnClickListener(v2 -> checkPaymentStatus());
            createPayment();
        });
    }

    private void loadQrCode(String qrCodeUrl) {
        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            showQrLoading(false);
            tvQrStatus.setText("❌ Không có mã QR");
            return;
        }

        // Load QR code image from URL
        CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(qrCodeUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e(TAG, "Error loading QR code", e);
                return null;
            }
        }).thenAccept(bitmap -> {
            runOnUiThread(() -> {
                if (bitmap != null) {
                    ivQrCode.setImageBitmap(bitmap);
                    showQrLoading(false);
                    tvQrStatus.setText("✅ Quét mã QR để thanh toán");
                } else {
                    showQrLoading(false);
                    tvQrStatus.setText("❌ Không thể tải mã QR");
                }
            });
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
        }
    }
    
    // Method để tạo mock payment cho testing
    private void createMockPaymentForTesting() {
        Toast.makeText(this, "Creating mock payment for testing...", Toast.LENGTH_SHORT).show();
        
        showQrLoading(true);
        tvQrStatus.setText("Đang tạo mock QR để test...");
        
        // Tạo mock payment
        Payment mockPayment = new Payment(
            userId, 
            PayOSConfig.DEFAULT_POST_QUOTA, 
            BigDecimal.valueOf(amount),
            description + " (MOCK)"
        );
        
        String orderCode = "MOCK_" + System.currentTimeMillis();
        mockPayment.setOrderCode(orderCode);
        mockPayment.setPayosOrderId(orderCode);
        mockPayment.setQrCodeUrl("https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=MOCK_PAYMENT_TEST");
        mockPayment.setStatus(Payment.PaymentStatus.PENDING);
        
        currentPayment = mockPayment;
        loadQrCode(mockPayment.getQrCodeUrl());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopStatusChecking();
    }
}
