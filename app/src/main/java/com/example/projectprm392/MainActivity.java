package com.example.projectprm392;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projectprm392.activities.HomeActivity;
import com.example.projectprm392.activities.admin.AdminDashboardActivity;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.views.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private LoginController loginController;
    private SessionManager sessionManager;
    private TextView tvWelcome;
    private MaterialButton btnLogout;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupToolbar();
        setupController();
        checkLoginStatus();
        
        // Check user role and redirect accordingly
        redirectToAppropriateActivity();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        
        // Setup logout button click listener
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupController() {
        loginController = new LoginController(this);
        sessionManager = new SessionManager(this);
    }

    private void redirectToAppropriateActivity() {
        if (!loginController.isLoggedIn()) {
            // User not logged in, redirect to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get user role from session
        String userRole = sessionManager.getRole();
        
        if ("ADMIN".equals(userRole) || "admin".equals(userRole)) {
            // Redirect to Admin Dashboard
            Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Redirect to HomeActivity for regular users
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void checkLoginStatus() {
        if (!loginController.isLoggedIn()) {
            return; // Will be handled in redirectToAppropriateActivity
        }

        // User is logged in, display welcome message
        User currentUser = loginController.getCurrentUser();
        if (currentUser != null) {
            String welcomeMessage = "Chào mừng, " + currentUser.getFullName() + "!\nVai trò: " + 
                (currentUser.getRole().equals("worker") ? "Người lao động" : 
                 currentUser.getRole().equals("employer") ? "Nhà tuyển dụng" : "Quản trị viên");
            tvWelcome.setText(welcomeMessage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        loginController.logout();
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}