package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectprm392.R;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.utils.EmailService;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {
    
    private TextInputEditText etEmail;
    private Button btnSendReset;
    private ProgressBar progressBar;
    private TextView tvStatus, tvBackToLogin;
    private ImageButton btnBack;
    
    private LoginController loginController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        initViews();
        initController();
        setupListeners();
        
        // Pre-fill email if passed from LoginActivity
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void initController() {
        loginController = new LoginController(this);
    }
    
    private void setupListeners() {
        btnSendReset.setOnClickListener(v -> sendResetCode());
        tvBackToLogin.setOnClickListener(v -> navigateToLogin());
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void sendResetCode() {
        String email = etEmail.getText().toString().trim();
        
        // Validation
        if (!validateEmail(email)) {
            return;
        }
        
        // Check if email exists in database
        showLoading(true);
        
        loginController.checkEmailExists(email, new LoginController.EmailExistsCallback() {
            @Override
            public void onEmailExists(String fullName) {
                // Email exists, send reset code
                sendPasswordResetEmail(email, fullName);
            }
            
            @Override
            public void onEmailNotExists() {
                runOnUiThread(() -> {
                    showLoading(false);
                    etEmail.setError("Email không tồn tại trong hệ thống");
                    showStatus("Email không tồn tại trong hệ thống", false);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showStatus("Lỗi kiểm tra email: " + error, false);
                });
            }
        });
    }
    
    private void sendPasswordResetEmail(String email, String fullName) {
        EmailService.sendPasswordResetEmailWithContext(this, email, fullName, new EmailService.EmailCallback() {
            @Override
            public void onSuccess(String resetCode) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showStatus("Mã đặt lại mật khẩu đã được gửi đến email của bạn", true);
                    
                    // Save reset code to database with expiry
                    loginController.savePasswordResetCode(email, resetCode);
                    
                    // Navigate to reset password activity
                    Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("resetCode", resetCode); // For debugging
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showStatus("Lỗi gửi email: " + error, false);
                });
            }
        });
    }
    
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSendReset.setEnabled(!show);
        btnSendReset.setText(show ? "Đang gửi..." : "Gửi mã đặt lại");
    }
    
    private void showStatus(String message, boolean isSuccess) {
        tvStatus.setText(message);
        tvStatus.setTextColor(getColor(isSuccess ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        tvStatus.setVisibility(View.VISIBLE);
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
