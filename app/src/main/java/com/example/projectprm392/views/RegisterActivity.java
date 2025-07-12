package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectprm392.R;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.viewmodels.LoginResponse;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.EmailService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilDescription;
    private TextInputEditText etFullName, etEmail, etPassword, etDescription;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginLink;
    private ImageButton btnBack;

    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupController();
        setupListeners();
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilDescription = findViewById(R.id.tilDescription);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etDescription = findViewById(R.id.etDescription);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        btnBack = findViewById(R.id.btnBack);
        
        // Check if btnRegister is found
        if (btnRegister == null) {
            Log.e(TAG, "btnRegister is null! Check layout ID");
        }
    }

    private void setupController() {
        loginController = new LoginController(this);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> {
            register();
        });

        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void register() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Clear previous errors
        clearAllErrors();

        // Validate inputs with improved validation
        if (!validateInputs(fullName, email, password)) {
            return;
        }

        // Check gender selection
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user object with default role "worker"
        User user = createUserFromInput(fullName, email, password, description, selectedGenderId);

        showLoading(true);

        loginController.register(user, new LoginController.RegisterCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                String email = etEmail.getText().toString().trim();
                String fullName = etFullName.getText().toString().trim();
                
                // Gửi email verification
                loginController.sendVerificationEmail(email, fullName, new EmailService.EmailCallback() {
                    @Override
                    public void onSuccess(String verificationCode) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Kiểm tra email để xác thực tài khoản.", Toast.LENGTH_LONG).show();
                            
                            // Chuyển đến EmailVerificationActivity
                            Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("verificationCode", verificationCode);
                            startActivity(intent);
                            finish();
                        });
                    }
                    
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            // Vẫn cho phép chuyển đến verification screen
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công. Đang gửi email xác thực...", Toast.LENGTH_LONG).show();
                            
                            // Retry sending email hoặc chuyển đến verification screen
                            Intent intent = new Intent(RegisterActivity.this, EmailVerificationActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("fullName", fullName);
                            intent.putExtra("verificationCode", ""); // Empty code, user will need to resend
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    handleRegistrationError(error);
                });
            }
        });
    }
    
    private void clearAllErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilDescription.setError(null);
    }
    
    private boolean validateInputs(String fullName, String email, String password) {
        boolean isValid = true;
        
        // Validate full name
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            isValid = false;
        } else if (fullName.length() < 2) {
            tilFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            etFullName.requestFocus();
            isValid = false;
        } else if (fullName.length() > 50) {
            tilFullName.setError("Họ và tên không được quá 50 ký tự");
            etFullName.requestFocus();
            isValid = false;
        } else if (!fullName.matches("^[a-zA-ZÀ-ỹ\\s]+$")) {
            tilFullName.setError("Họ và tên chỉ được chứa chữ cái và khoảng trắng");
            etFullName.requestFocus();
            isValid = false;
        }

        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            if (isValid) etEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            if (isValid) etEmail.requestFocus();
            isValid = false;
        } else if (email.length() > 100) {
            tilEmail.setError("Email không được quá 100 ký tự");
            if (isValid) etEmail.requestFocus();
            isValid = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        } else if (password.length() > 50) {
            tilPassword.setError("Mật khẩu không được quá 50 ký tự");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        } else if (!isPasswordStrong(password)) {
            tilPassword.setError("Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số");
            if (isValid) etPassword.requestFocus();
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean isPasswordStrong(String password) {
        // Check if password contains at least one letter and one digit
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }
    
    private User createUserFromInput(String fullName, String email, String password, String description, 
                                    int selectedGenderId) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email.toLowerCase()); // Normalize email to lowercase
        user.setPassword(password);
        user.setDescription(TextUtils.isEmpty(description) ? null : description);

        // Set gender
        user.setGender(selectedGenderId == R.id.rbMale);

        // Set default role as worker
        user.setRole("worker");
        
        // Set default post quota to 0
        user.setPostQuota(0);
        
        // Set initial verification status
        user.setVerified(false);
        
        return user;
    }
    
    private void handleRegistrationError(String error) {
        // Handle specific error cases
        if (error.contains("Email đã được sử dụng") || error.contains("email") && error.contains("exist")) {
            tilEmail.setError("Email này đã được đăng ký");
            etEmail.requestFocus();
            Toast.makeText(this, "Email đã được sử dụng. Vui lòng sử dụng email khác hoặc đăng nhập.", Toast.LENGTH_LONG).show();
        } else if (error.contains("mật khẩu") || error.contains("password")) {
            tilPassword.setError("Mật khẩu không hợp lệ");
            etPassword.requestFocus();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        } else if (error.contains("tên") || error.contains("name")) {
            tilFullName.setError("Tên không hợp lệ");
            etFullName.requestFocus();
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        } else {
            // Generic error
            Toast.makeText(this, "Lỗi đăng ký: " + error, Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
