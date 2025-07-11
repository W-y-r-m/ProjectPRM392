package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectprm392.R;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.utils.EmailService;
import com.google.android.material.textfield.TextInputEditText;

public class EmailVerificationActivity extends AppCompatActivity {
    
    private TextInputEditText etVerificationCode;
    private Button btnVerify;
    private TextView tvEmail, tvResendCode, tvResendTimer;
    private ProgressBar progressBar;
    
    private String userEmail;
    private String userName;
    private String expectedCode;
    private LoginController loginController;
    
    private CountDownTimer resendTimer;
    private static final long RESEND_COUNTDOWN = 60000; // 60 seconds
    private boolean canResend = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);
        
        initViews();
        getIntentData();
        setupListeners();
        startResendTimer();
        
        loginController = new LoginController(this);
    }
    
    private void initViews() {
        etVerificationCode = findViewById(R.id.etVerificationCode);
        btnVerify = findViewById(R.id.btnVerify);
        tvEmail = findViewById(R.id.tvEmail);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvResendTimer = findViewById(R.id.tvResendTimer);
        progressBar = findViewById(R.id.progressBar);
        
        // Initialize timer text view if it doesn't exist in layout
        if (tvResendTimer == null) {
            tvResendTimer = new TextView(this);
            tvResendTimer.setTextSize(14);
            tvResendTimer.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("email");
        userName = intent.getStringExtra("fullName");
        expectedCode = intent.getStringExtra("verificationCode");
        
        if (userEmail != null) {
            tvEmail.setText(userEmail);
        }
        
        if (userName == null || userName.trim().isEmpty()) {
            userName = "Người dùng";
        }
    }
    
    private void setupListeners() {
        // Auto verify when 6 digits entered
        etVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear any previous errors
                etVerificationCode.setError(null);
                
                if (s.length() == 6) {
                    // Auto verify when 6 digits are entered
                    verifyCode();
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        btnVerify.setOnClickListener(v -> verifyCode());
        
        tvResendCode.setOnClickListener(v -> {
            if (canResend) {
                resendVerificationCode();
            } else {
                Toast.makeText(this, "Vui lòng đợi trước khi gửi lại mã", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void verifyCode() {
        String enteredCode = etVerificationCode.getText().toString().trim();
        
        if (enteredCode.length() != 6) {
            etVerificationCode.setError("Vui lòng nhập đầy đủ 6 chữ số");
            Toast.makeText(this, "Vui lòng nhập đầy đủ 6 chữ số", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate that code contains only digits
        if (!enteredCode.matches("\\d{6}")) {
            etVerificationCode.setError("Mã xác thực chỉ chứa số");
            Toast.makeText(this, "Mã xác thực chỉ chứa số", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Verify with backend
        loginController.verifyEmail(userEmail, enteredCode, new LoginController.VerificationCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    stopResendTimer();
                    Toast.makeText(EmailVerificationActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Chuyển về LoginActivity với email đã fill
                    Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
                    intent.putExtra("email", userEmail);
                    intent.putExtra("verified", true);
                    intent.putExtra("message", "Tài khoản đã được xác thực thành công. Vui lòng đăng nhập.");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    etVerificationCode.setError("Mã xác thực không đúng");
                    Toast.makeText(EmailVerificationActivity.this, error, Toast.LENGTH_LONG).show();
                    etVerificationCode.setText("");
                    etVerificationCode.requestFocus();
                });
            }
        });
    }
    
    private void resendVerificationCode() {
        if (!canResend) {
            return;
        }
        
        showLoading(true);
        canResend = false;
        tvResendCode.setEnabled(false);
        
        EmailService.sendVerificationEmailWithContext(this, userEmail, userName, new EmailService.EmailCallback() {
            @Override
            public void onSuccess(String verificationCode) {
                runOnUiThread(() -> {
                    showLoading(false);
                    expectedCode = verificationCode;
                    Toast.makeText(EmailVerificationActivity.this, "Đã gửi lại mã xác thực", Toast.LENGTH_SHORT).show();
                    
                    // Update code in database
                    loginController.updateVerificationCode(userEmail, verificationCode);
                    
                    // Start countdown timer again
                    startResendTimer();
                    
                    // Clear current input and focus
                    etVerificationCode.setText("");
                    etVerificationCode.requestFocus();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    canResend = true;
                    tvResendCode.setEnabled(true);
                    Toast.makeText(EmailVerificationActivity.this, "Lỗi gửi email: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private void startResendTimer() {
        canResend = false;
        tvResendCode.setEnabled(false);
        tvResendCode.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        
        resendTimer = new CountDownTimer(RESEND_COUNTDOWN, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvResendCode.setText(String.format("Gửi lại (%d)", seconds));
            }
            
            @Override
            public void onFinish() {
                canResend = true;
                tvResendCode.setEnabled(true);
                tvResendCode.setText("Gửi lại mã");
                tvResendCode.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));
            }
        };
        
        resendTimer.start();
    }
    
    private void stopResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
            resendTimer = null;
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!show);
        etVerificationCode.setEnabled(!show);
        
        if (!show && canResend) {
            tvResendCode.setEnabled(true);
        }
    }
    
    @Override
    public void onBackPressed() {
        // Có thể quay lại RegisterActivity hoặc hỏi người dùng
        super.onBackPressed();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopResendTimer();
    }
}
