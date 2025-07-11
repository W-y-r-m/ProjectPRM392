package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectprm392.MainActivity;
import com.example.projectprm392.R;
import com.example.projectprm392.activities.HomeActivity;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.viewmodels.LoginResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegisterLink, tvForgotPassword;

    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupController();
        setupListeners();
        
        // Check if user is already logged in
        if (loginController.isLoggedIn()) {
            navigateToHomeActivity();
            return;
        }
        
        // Check if there's a verification success message
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra("email");
            if (email != null) {
                etEmail.setText(email);
            }
            
            boolean isVerified = intent.getBooleanExtra("verified", false);
            String message = intent.getStringExtra("message");
            
            if (isVerified && message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupController() {
        loginController = new LoginController(this);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        loginController.login(email, password, new LoginController.LoginCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    navigateToHomeActivity();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    // Handle specific error cases
                    if (error.contains("chưa được xác thực") || error.contains("not verified")) {
                        // Show verification prompt with option to resend email
                        showVerificationDialog(email);
                    } else if (error.contains("Mật khẩu")) {
                        tilPassword.setError("Mật khẩu không chính xác");
                        etPassword.requestFocus();
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    } else if (error.contains("không tồn tại")) {
                        tilEmail.setError("Tài khoản không tồn tại");
                        etEmail.requestFocus();
                        Toast.makeText(LoginActivity.this, error + " Vui lòng đăng ký tài khoản mới.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showVerificationDialog(String email) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Tài khoản chưa xác thực");
        builder.setMessage("Tài khoản của bạn chưa được xác thực email. Bạn có muốn gửi lại email xác thực không?");
        
        builder.setPositiveButton("Gửi lại email", (dialog, which) -> {
            // Get user info and resend verification email
            resendVerificationEmail(email);
        });
        
        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
        });
        
        builder.setNeutralButton("Đăng ký lại", (dialog, which) -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        builder.show();
    }
    
    private void resendVerificationEmail(String email) {
        showLoading(true);
        
        // We need to get the user's full name first, so we'll use a simplified approach
        // In a real implementation, you might want to have an API to resend verification without full name
        loginController.sendVerificationEmail(email, "Người dùng", new com.example.projectprm392.utils.EmailService.EmailCallback() {
            @Override
            public void onSuccess(String verificationCode) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Đã gửi email xác thực. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                    
                    // Navigate to verification screen
                    Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("fullName", "Người dùng");
                    intent.putExtra("verificationCode", verificationCode);
                    startActivity(intent);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi gửi email: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
