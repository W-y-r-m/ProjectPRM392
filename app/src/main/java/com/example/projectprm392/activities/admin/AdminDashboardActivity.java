package com.example.projectprm392.activities.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.projectprm392.R;
import com.example.projectprm392.activities.admin.AdminUserManagementActivity;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.views.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWelcomeAdmin, tvTotalUsers, tvTotalJobs, tvTotalReports, tvTotalApplications;
    private CardView cardUserManagement, cardPostManagement, cardReportManagement, cardApplicationManagement;
    private MaterialButton btnRefreshStats;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private LoginController loginController;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupToolbar();
        setupListeners();
        
        // Auto initialize sample data if needed
        autoInitializeSampleData();
        
        loadStatistics();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalJobs = findViewById(R.id.tvTotalJobs);
        tvTotalReports = findViewById(R.id.tvTotalReports);
        tvTotalApplications = findViewById(R.id.tvTotalApplications);
        
        cardUserManagement = findViewById(R.id.cardUserManagement);
        cardPostManagement = findViewById(R.id.cardPostManagement);
        cardReportManagement = findViewById(R.id.cardReportManagement);
        cardApplicationManagement = findViewById(R.id.cardApplicationManagement);
        
        btnRefreshStats = findViewById(R.id.btnRefreshStats);
        
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        loginController = new LoginController(this);
        executorService = Executors.newCachedThreadPool();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }
        
        // Set welcome message
        String adminName = sessionManager.getFullName();
        tvWelcomeAdmin.setText("Chào mừng, " + (adminName != null ? adminName : "Admin"));
    }

    private void setupListeners() {
        cardUserManagement.setOnClickListener(v -> {
            try {
                Toast.makeText(this, "Đang mở Quản lý người dùng...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminUserManagementActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                android.util.Log.e("AdminDashboard", "Error starting UserManagement: " + e.getMessage(), e);
            }
        });

        cardPostManagement.setOnClickListener(v -> {
            try {
                Toast.makeText(this, "Đang mở Quản lý bài đăng...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AdminPostManagementActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                android.util.Log.e("AdminDashboard", "Error starting PostManagement: " + e.getMessage(), e);
            }
        });

        cardReportManagement.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, AdminReportManagementActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("AdminDashboard", "Error starting AdminReportManagementActivity", e);
                Toast.makeText(this, "Lỗi mở quản lý báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        cardApplicationManagement.setOnClickListener(v -> {
            // TODO: Implement AdminApplicationManagementActivity
            Toast.makeText(this, "Chức năng quản lý ứng tuyển đang phát triển", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, AdminApplicationManagementActivity.class);
            // startActivity(intent);
        });

        btnRefreshStats.setOnClickListener(v -> {
            loadStatistics();
            Toast.makeText(this, "Đã cập nhật thống kê", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadStatistics() {
        try {
            // Load statistics from database
            int totalUsers = databaseHelper.getAllUsers().size();
            int totalJobs = databaseHelper.getAllJobs().size();
            int totalReports = databaseHelper.getAllReports().size();
            int totalApplications = databaseHelper.getAllApplications().size();

            // Update UI
            tvTotalUsers.setText(String.valueOf(totalUsers));
            tvTotalJobs.setText(String.valueOf(totalJobs));
            tvTotalReports.setText(String.valueOf(totalReports));
            tvTotalApplications.setText(String.valueOf(totalApplications));

            // Only show toast when manually refreshed
            // Toast.makeText(this, "Đã cập nhật thống kê", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void autoInitializeSampleData() {
        executorService.execute(() -> {
            try {
                // Kiểm tra xem đã có dữ liệu chưa
                int totalUsers = databaseHelper.getAllUsers().size();
                int totalJobs = databaseHelper.getAllJobs().size();
                int totalApplications = databaseHelper.getAllApplications().size();
                int totalReports = databaseHelper.getAllReports().size();
                
                // Nếu chưa có dữ liệu hoặc có ít dữ liệu thì tự động tạo
                if (totalUsers <= 1 || totalJobs == 0 || totalApplications == 0 || totalReports == 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "🔄 Đang tự động tạo dữ liệu mẫu...", Toast.LENGTH_SHORT).show();
                    });
                    
                    // Tự động reset và tạo dữ liệu mẫu
                    databaseHelper.resetAndInitializeSampleData();
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "✅ Đã tạo dữ liệu mẫu thành công!", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "⚠️ Lỗi tạo dữ liệu tự động: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_settings) {
            Toast.makeText(this, "Chức năng cài đặt đang phát triển", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_refresh) {
            loadStatistics();
            return true;
        } else if (id == R.id.action_admin_profile) {
            showAdminProfile();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showAdminProfile() {
        try {
            // Lấy thông tin admin từ session hoặc database
            String adminEmail = sessionManager.getEmail();
            String adminName = sessionManager.getFullName();
            String adminRole = sessionManager.getRole();
            
            // Lấy thêm thông tin chi tiết từ database
            executorService.execute(() -> {
                try {
                    com.example.projectprm392.database.UserEntity adminUser = databaseHelper.getUserByEmail(adminEmail);
                    
                    runOnUiThread(() -> {
                        showAdminProfileDialog(adminUser);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi tải thông tin admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi hiển thị thông tin admin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showAdminProfileDialog(com.example.projectprm392.database.UserEntity adminUser) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        
        // Tạo nội dung thông tin admin
        String adminInfo = buildAdminInfoText(adminUser);
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("👤 Thông tin Admin");
        builder.setMessage(adminInfo);
        builder.setIcon(R.drawable.ic_person);
        
        // Nút chỉnh sửa thông tin
        builder.setPositiveButton("✏️ Chỉnh sửa", (dialog, which) -> {
            showEditAdminDialog(adminUser);
        });
        
        // Nút đổi mật khẩu
        builder.setNeutralButton("🔒 Đổi mật khẩu", (dialog, which) -> {
            showChangePasswordDialog(adminUser);
        });
        
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
    
    private String buildAdminInfoText(com.example.projectprm392.database.UserEntity adminUser) {
        if (adminUser == null) {
            return "❌ Không tìm thấy thông tin admin";
        }
        
        StringBuilder info = new StringBuilder();
        info.append("📧 Email: ").append(adminUser.getEmail()).append("\n\n");
        info.append("👤 Họ tên: ").append(adminUser.getFullName() != null ? adminUser.getFullName() : "Chưa cập nhật").append("\n\n");
        info.append("💼 Vai trò: ").append(adminUser.getRole()).append("\n\n");
        info.append("📱 Số điện thoại: ").append(adminUser.getPhoneNumber() != null ? adminUser.getPhoneNumber() : "Chưa cập nhật").append("\n\n");
        
        // Giới tính
        String gender = "Chưa cập nhật";
        if (adminUser.getGender() != null) {
            gender = adminUser.getGender() ? "Nam" : "Nữ";
        }
        info.append("👥 Giới tính: ").append(gender).append("\n\n");
        
        // Trạng thái
        info.append("✅ Trạng thái: ").append(adminUser.getIsActive() ? "Đang hoạt động" : "Đã khóa").append("\n\n");
        info.append("🔍 Xác thực: ").append(adminUser.getIsVerified() ? "Đã xác thực" : "Chưa xác thực").append("\n\n");
        
        // Ngày tạo
        if (adminUser.getCreatedAt() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            info.append("📅 Ngày tạo: ").append(sdf.format(adminUser.getCreatedAt())).append("\n\n");
        }
        
        // Mô tả
        if (adminUser.getDescription() != null && !adminUser.getDescription().trim().isEmpty()) {
            info.append("📝 Mô tả: ").append(adminUser.getDescription());
        }
        
        return info.toString();
    }
    
    private void showEditAdminDialog(com.example.projectprm392.database.UserEntity adminUser) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_user_form, null);
        
        // Lấy các view trong dialog
        com.google.android.material.textfield.TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        android.widget.RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        
        // Ẩn các field không cần thiết
        dialogView.findViewById(R.id.tilEmail).setVisibility(android.view.View.GONE);
        dialogView.findViewById(R.id.tilPassword).setVisibility(android.view.View.GONE);
        dialogView.findViewById(R.id.tilRole).setVisibility(android.view.View.GONE);
        dialogView.findViewById(R.id.tilPostQuota).setVisibility(android.view.View.GONE);
        dialogView.findViewById(R.id.cbVerified).setVisibility(android.view.View.GONE);
        dialogView.findViewById(R.id.cbActive).setVisibility(android.view.View.GONE);
        
        // Điền thông tin hiện tại
        etFullName.setText(adminUser.getFullName());
        etPhone.setText(adminUser.getPhoneNumber());
        etDescription.setText(adminUser.getDescription());
        
        if (adminUser.getGender() != null) {
            if (adminUser.getGender()) {
                rgGender.check(R.id.rbMale);
            } else {
                rgGender.check(R.id.rbFemale);
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("✏️ Chỉnh sửa thông tin Admin");
        
        builder.setPositiveButton("💾 Lưu", (dialog, which) -> {
            // Cập nhật thông tin admin
            updateAdminInfo(adminUser, etFullName.getText().toString().trim(),
                    etPhone.getText().toString().trim(),
                    etDescription.getText().toString().trim(),
                    rgGender.getCheckedRadioButtonId());
        });
        
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    
    private void showChangePasswordDialog(com.example.projectprm392.database.UserEntity adminUser) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        
        // Tạo layout đơn giản cho đổi mật khẩu
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        com.google.android.material.textfield.TextInputLayout tilOldPassword = new com.google.android.material.textfield.TextInputLayout(this);
        com.google.android.material.textfield.TextInputEditText etOldPassword = new com.google.android.material.textfield.TextInputEditText(this);
        etOldPassword.setHint("Mật khẩu hiện tại");
        etOldPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilOldPassword.addView(etOldPassword);
        
        com.google.android.material.textfield.TextInputLayout tilNewPassword = new com.google.android.material.textfield.TextInputLayout(this);
        com.google.android.material.textfield.TextInputEditText etNewPassword = new com.google.android.material.textfield.TextInputEditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilNewPassword.addView(etNewPassword);
        
        com.google.android.material.textfield.TextInputLayout tilConfirmPassword = new com.google.android.material.textfield.TextInputLayout(this);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = new com.google.android.material.textfield.TextInputEditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilConfirmPassword.addView(etConfirmPassword);
        
        layout.addView(tilOldPassword);
        layout.addView(tilNewPassword);
        layout.addView(tilConfirmPassword);
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle("🔒 Đổi mật khẩu Admin");
        
        builder.setPositiveButton("💾 Đổi mật khẩu", (dialog, which) -> {
            String oldPassword = etOldPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (validatePasswordChange(oldPassword, newPassword, confirmPassword)) {
                changeAdminPassword(adminUser, oldPassword, newPassword);
            }
        });
        
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    
    private void updateAdminInfo(com.example.projectprm392.database.UserEntity adminUser, String fullName, String phone, String description, int genderId) {
        try {
            // Cập nhật thông tin
            adminUser.setFullName(fullName);
            adminUser.setPhoneNumber(phone);
            adminUser.setDescription(description);
            
            // Cập nhật giới tính
            if (genderId == R.id.rbMale) {
                adminUser.setGender(true);
            } else if (genderId == R.id.rbFemale) {
                adminUser.setGender(false);
            }
            
            // Lưu vào database (cần implement method updateUser trong DatabaseHelper)
            // databaseHelper.updateUser(adminUser);
            
            // Cập nhật session (sử dụng method có sẵn)
            // sessionManager.saveFullName(fullName);
            
            Toast.makeText(this, "✅ Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
            
            // Cập nhật lại welcome message
            tvWelcomeAdmin.setText("Chào mừng, " + fullName);
            
        } catch (Exception e) {
            Toast.makeText(this, "❌ Lỗi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private boolean validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
        if (oldPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private void changeAdminPassword(com.example.projectprm392.database.UserEntity adminUser, String oldPassword, String newPassword) {
        try {
            // Debug: Kiểm tra mật khẩu hiện tại trong database
            String currentDbPassword = databaseHelper.getCurrentPassword(adminUser.getEmail());
            android.util.Log.d("AdminDashboard", "Current password in DB: " + currentDbPassword);
            android.util.Log.d("AdminDashboard", "Old password entered: " + oldPassword);
            android.util.Log.d("AdminDashboard", "New password to set: " + newPassword);
            
            // Kiểm tra mật khẩu cũ trực tiếp với database
            if (!oldPassword.equals(currentDbPassword)) {
                Toast.makeText(this, "❌ Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Cập nhật mật khẩu mới trực tiếp
            loginController.updatePassword(adminUser.getEmail(), newPassword, new LoginController.UpdatePasswordCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        // Debug: Kiểm tra mật khẩu sau khi update
                        String updatedPassword = databaseHelper.getCurrentPassword(adminUser.getEmail());
                        android.util.Log.d("AdminDashboard", "Password after update: " + updatedPassword);
                        
                        Toast.makeText(AdminDashboardActivity.this, 
                            "✅ Đổi mật khẩu thành công! Mật khẩu mới: " + newPassword + ". Bạn sẽ được đăng xuất để sử dụng mật khẩu mới.", 
                            Toast.LENGTH_LONG).show();
                        
                        // Đăng xuất ngay sau khi đổi mật khẩu thành công để buộc sử dụng mật khẩu mới
                        new android.os.Handler().postDelayed(() -> {
                            logout();
                        }, 3000); // Delay 3 giây để user đọc thông báo
                    });
                }
                
                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(AdminDashboardActivity.this, 
                            "❌ Lỗi cập nhật mật khẩu: " + error, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
            
        } catch (Exception e) {
            android.util.Log.e("AdminDashboard", "Error changing password", e);
            Toast.makeText(this, "❌ Lỗi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        sessionManager.logout();
        Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to dashboard
        loadStatistics();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
