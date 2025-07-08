package com.example.projectprm392.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.example.projectprm392.models.LoginResponse;
import com.example.projectprm392.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPassword, tilDescription;
    private TextInputEditText etFullName, etEmail, etPassword, etDescription;
    private RadioGroup rgGender, rgRole;
    private RadioButton rbMale, rbFemale, rbWorker, rbEmployer;
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
        rgRole = findViewById(R.id.rgRole);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbWorker = findViewById(R.id.rbWorker);
        rbEmployer = findViewById(R.id.rbEmployer);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupController() {
        loginController = new LoginController(this);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());

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
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            tilFullName.setError("Họ và tên phải có ít nhất 2 ký tự");
            etFullName.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        // Check gender selection
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check role selection
        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Vui lòng chọn vai trò", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user object
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setDescription(TextUtils.isEmpty(description) ? null : description);

        // Set gender
        user.setGender(selectedGenderId == R.id.rbMale);

        // Set role
        String role = selectedRoleId == R.id.rbWorker ? "worker" : "employer";
        user.setRole(role);

        showLoading(true);

        loginController.register(user, new LoginController.RegisterCallback() {
            @Override
            public void onSuccess(LoginResponse response) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, response.getMessage(), Toast.LENGTH_LONG).show();
                    
                    // Navigate to login activity with email pre-filled
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
