package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
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

public class ResetPasswordActivity extends AppCompatActivity {
    
    private TextInputEditText etResetCode, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private TextView tvEmailInfo, tvResendCode;
    private ImageButton btnBack;
    
    private String userEmail;
    private String expectedResetCode;
    private LoginController loginController;
    
    private CountDownTimer resendTimer;
    private static final long RESEND_COUNTDOWN = 60000; // 60 seconds
    private boolean canResend = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        
        initViews();
        initController();
        getIntentData();
        setupListeners();
        startResendTimer();
    }
    
    private void initViews() {
        etResetCode = findViewById(R.id.etResetCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        tvEmailInfo = findViewById(R.id.tvEmailInfo);
        tvResendCode = findViewById(R.id.tvResendCode);
        btnBack = findViewById(R.id.btnBack);
    }
    
    private void initController() {
        loginController = new LoginController(this);
    }
    
    private void getIntentData() {
        userEmail = getIntent().getStringExtra("email");
        expectedResetCode = getIntent().getStringExtra("resetCode");
        
        if (userEmail != null) {
            tvEmailInfo.setText("Mã đã được gửi đến " + maskEmail(userEmail));
        }
    }
    
    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> resetPassword());
        tvResendCode.setOnClickListener(v -> resendResetCode());
        btnBack.setOnClickListener(v -> finish());
        
        // Auto-submit when reset code is entered
        etResetCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 6) {
                    etNewPassword.requestFocus();
                }
            }
        });
    }
    
    private void resetPassword() {
        String resetCode = etResetCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validation
        if (!validateInputs(resetCode, newPassword, confirmPassword)) {
            return;
        }
        
        showLoading(true);
        
        // Verify reset code
        loginController.verifyPasswordResetCode(userEmail, resetCode, new LoginController.VerificationCallback() {
            @Override
            public void onSuccess() {
                // Reset code is valid, update password
                updatePassword(newPassword);
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    etResetCode.setError("Mã xác thực không đúng hoặc đã hết hạn");
                    Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_LONG).show();
                    etResetCode.setText("");
                    etResetCode.requestFocus();
                });
            }
        });
    }
    
    private void updatePassword(String newPassword) {
        loginController.updatePassword(userEmail, newPassword, new LoginController.UpdatePasswordCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    stopResendTimer();
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to login with success message
                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.putExtra("email", userEmail);
                    intent.putExtra("message", "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập với mật khẩu mới.");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi cập nhật mật khẩu: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    private boolean validateInputs(String resetCode, String newPassword, String confirmPassword) {
        // Validate reset code
        if (TextUtils.isEmpty(resetCode)) {
            etResetCode.setError("Vui lòng nhập mã xác thực");
            etResetCode.requestFocus();
            return false;
        }
        
        if (resetCode.length() != 6) {
            etResetCode.setError("Mã xác thực phải có 6 số");
            etResetCode.requestFocus();
            return false;
        }
        
        if (!resetCode.matches("\\d{6}")) {
            etResetCode.setError("Mã xác thực chỉ chứa số");
            etResetCode.requestFocus();
            return false;
        }
        
        // Validate new password
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return false;
        }
        
        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return false;
        }
        
        // Validate confirm password
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void resendResetCode() {
        if (!canResend) {
            return;
        }
        
        loginController.getUserFullName(userEmail, new LoginController.UserInfoCallback() {
            @Override
            public void onSuccess(String fullName) {
                // Send new reset code
                EmailService.sendPasswordResetEmailWithContext(ResetPasswordActivity.this, userEmail, fullName, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess(String resetCode) {
                        runOnUiThread(() -> {
                            expectedResetCode = resetCode;
                            Toast.makeText(ResetPasswordActivity.this, "Đã gửi lại mã xác thực", Toast.LENGTH_SHORT).show();
                            
                            // Update code in database
                            loginController.savePasswordResetCode(userEmail, resetCode);
                            
                            // Start countdown timer again
                            startResendTimer();
                            
                            // Clear current input and focus
                            etResetCode.setText("");
                            etResetCode.requestFocus();
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            canResend = true;
                            tvResendCode.setEnabled(true);
                            Toast.makeText(ResetPasswordActivity.this, "Lỗi gửi email: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi lấy thông tin user: " + error, Toast.LENGTH_LONG).show();
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
                tvResendCode.setText("Gửi lại");
                tvResendCode.setTextColor(getResources().getColor(R.color.colorPrimary, null));
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
        btnResetPassword.setEnabled(!show);
        btnResetPassword.setText(show ? "Đang đặt lại..." : "Đặt lại mật khẩu");
    }
    
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) return email;
        
        String[] parts = email.split("@");
        if (parts.length != 2) return email;
        
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username + "@" + domain;
        }
        
        String masked = username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1);
        return masked + "@" + domain;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopResendTimer();
    }
}
