package com.example.projectprm392.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projectprm392.MainActivity;
import com.example.projectprm392.R;
import com.google.android.material.button.MaterialButton;

public class PaymentSuccessActivity extends AppCompatActivity {
    
    // Intent extras
    public static final String EXTRA_PAYMENT_ID = "payment_id";
    public static final String EXTRA_AMOUNT = "amount";
    public static final String EXTRA_POST_QUOTA = "post_quota";
    
    // Views
    private Toolbar toolbar;
    private TextView tvAmount;
    private TextView tvPostQuota;
    private TextView tvMessage;
    private MaterialButton btnContinue;
    private MaterialButton btnViewHistory;
    
    // Data
    private String paymentId;
    private int amount;
    private int postQuota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);
        
        // Get intent data
        getIntentData();
        
        // Initialize views
        initViews();
        setupToolbar();
        setupData();
        setupListeners();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        paymentId = intent.getStringExtra(EXTRA_PAYMENT_ID);
        amount = intent.getIntExtra(EXTRA_AMOUNT, 0);
        postQuota = intent.getIntExtra(EXTRA_POST_QUOTA, 0);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAmount = findViewById(R.id.tvAmount);
        tvPostQuota = findViewById(R.id.tvPostQuota);
        tvMessage = findViewById(R.id.tvMessage);
        btnContinue = findViewById(R.id.btnContinue);
        btnViewHistory = findViewById(R.id.btnViewHistory);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // Không cho back
            getSupportActionBar().setTitle("Thanh toán thành công");
        }
    }

    private void setupData() {
        tvAmount.setText(String.format("%,dđ", amount));
        tvPostQuota.setText(String.format("%d lượt đăng tin", postQuota));
        tvMessage.setText("Chúc mừng! Bạn đã nạp tiền thành công và nhận được " + 
            postQuota + " lượt đăng tin việc làm.");
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            // Quay về trang chính hoặc trang đăng tin
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        btnViewHistory.setOnClickListener(v -> {
            // Mở trang lịch sử thanh toán (nếu có)
            // Intent historyIntent = new Intent(this, PaymentHistoryActivity.class);
            // startActivity(historyIntent);
            
            // Tạm thời chỉ finish activity này
            btnContinue.performClick();
        });
    }

    @Override
    public void onBackPressed() {
        // Không cho phép back, phải dùng nút Continue
        super.onBackPressed(); // Gọi super để tránh lint warning
        btnContinue.performClick();
    }
}
