package com.example.projectprm392.activities.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.admin.UserManagementAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminUserManagementActivity extends AppCompatActivity implements UserManagementAdapter.OnUserActionListener {

    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter, btnAddUser;
    private TextView tvUserCount, tvFilteredCount;
    private RecyclerView rvUsers;
    private View layoutEmptyState;
    private ProgressBar progressBar;

    private UserManagementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Toast.makeText(this, "Đang khởi tạo Quản lý người dùng...", Toast.LENGTH_SHORT).show();
            android.util.Log.d("AdminUserManagement", "Starting onCreate");
            setContentView(R.layout.activity_admin_user_management);
            android.util.Log.d("AdminUserManagement", "Content view set");
            
            initViews();
            android.util.Log.d("AdminUserManagement", "Views initialized");
            
            setupToolbar();
            android.util.Log.d("AdminUserManagement", "Toolbar setup");
            
            setupRecyclerView();
            android.util.Log.d("AdminUserManagement", "RecyclerView setup");
            
            setupListeners();
            android.util.Log.d("AdminUserManagement", "Listeners setup");
            
            loadUsers();
            android.util.Log.d("AdminUserManagement", "Users loaded");
            
            Toast.makeText(this, "Khởi tạo thành công!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("AdminUserManagement", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            etSearch = findViewById(R.id.etSearch);
            btnFilter = findViewById(R.id.btnFilter);
            btnAddUser = findViewById(R.id.btnAddUser);
            tvUserCount = findViewById(R.id.tvUserCount);
            tvFilteredCount = findViewById(R.id.tvFilteredCount);
            rvUsers = findViewById(R.id.rvUsers);
            layoutEmptyState = findViewById(R.id.layoutEmptyState);
            progressBar = findViewById(R.id.progressBar);

            // Check for null views
            if (toolbar == null) throw new RuntimeException("Toolbar not found");
            if (etSearch == null) throw new RuntimeException("Search EditText not found");
            if (btnFilter == null) throw new RuntimeException("Filter button not found");
            if (btnAddUser == null) throw new RuntimeException("Add button not found");
            if (rvUsers == null) throw new RuntimeException("RecyclerView not found");

            databaseHelper = new DatabaseHelper(this);
            executorService = Executors.newCachedThreadPool();
            
            android.util.Log.d("AdminUserManagement", "All views found successfully");
        } catch (Exception e) {
            android.util.Log.e("AdminUserManagement", "Error in initViews: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý người dùng");
        }
    }

    private void setupRecyclerView() {
        adapter = new UserManagementAdapter(this, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateCounts();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Add user button
        btnAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    private void loadUsers() {
        android.util.Log.d("AdminUserManagement", "Loading users from database...");
        showLoading(true);
        executorService.execute(() -> {
            try {
                List<UserEntity> users = databaseHelper.getAllUsers();
                android.util.Log.d("AdminUserManagement", "Loaded " + users.size() + " users");
                for (UserEntity user : users) {
                    android.util.Log.d("AdminUserManagement", "User: " + user.getEmail() + " - " + user.getFullName() + " - " + user.getRole());
                }
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.updateUsers(users);
                    updateCounts();
                    updateEmptyState();
                });
            } catch (Exception e) {
                android.util.Log.e("AdminUserManagement", "Error loading users: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateCounts() {
        List<UserEntity> allUsers = databaseHelper.getAllUsers();
        List<UserEntity> filteredUsers = adapter.getFilteredUsers();
        
        // Count only non-admin users for total
        int nonAdminCount = 0;
        for (UserEntity user : allUsers) {
            if (!"ADMIN".equals(user.getRole())) {
                nonAdminCount++;
            }
        }
        
        tvUserCount.setText("Tổng: " + nonAdminCount + " người dùng");
        tvFilteredCount.setText("Hiển thị: " + filteredUsers.size());
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvUsers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showFilterDialog() {
        String[] roles = {"Tất cả", "Quản trị viên", "Nhà tuyển dụng", "Người lao động"};
        String[] roleValues = {"ALL", "ADMIN", "EMPLOYER", "WORKER"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lọc theo vai trò");
        builder.setSingleChoiceItems(roles, getCurrentFilterIndex(), (dialog, which) -> {
            currentFilter = roleValues[which];
            adapter.filterByRole(currentFilter);
            updateCounts();
            updateEmptyState();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private int getCurrentFilterIndex() {
        switch (currentFilter) {
            case "ADMIN": return 1;
            case "EMPLOYER": return 2;
            case "WORKER": return 3;
            default: return 0;
        }
    }

    private void showAddUserDialog() {
        showUserFormDialog(null);
    }

    private void showUserFormDialog(UserEntity userToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user_form, null);
        
        // Get form elements
        TextView tvFormTitle = dialogView.findViewById(R.id.tvFormTitle);
        TextInputLayout tilEmail = dialogView.findViewById(R.id.tilEmail);
        TextInputLayout tilPassword = dialogView.findViewById(R.id.tilPassword);
        TextInputLayout tilFullName = dialogView.findViewById(R.id.tilFullName);
        TextInputLayout tilPhone = dialogView.findViewById(R.id.tilPhone);
        TextInputLayout tilRole = dialogView.findViewById(R.id.tilRole);
        TextInputLayout tilDescription = dialogView.findViewById(R.id.tilDescription);
        TextInputLayout tilPostQuota = dialogView.findViewById(R.id.tilPostQuota);
        
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        AutoCompleteTextView actvRole = dialogView.findViewById(R.id.actvRole);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPostQuota = dialogView.findViewById(R.id.etPostQuota);
        
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        CheckBox cbVerified = dialogView.findViewById(R.id.cbVerified);
        CheckBox cbActive = dialogView.findViewById(R.id.cbActive);
        
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup role dropdown
        String[] roles = {"WORKER", "EMPLOYER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        actvRole.setAdapter(roleAdapter);
        
        // Handle role selection to show/hide post quota
        actvRole.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRole = roles[position];
            tilPostQuota.setVisibility("EMPLOYER".equals(selectedRole) ? View.VISIBLE : View.GONE);
        });

        // Set form title and populate fields if editing
        boolean isEditing = userToEdit != null;
        tvFormTitle.setText(isEditing ? "Sửa thông tin người dùng" : "Thêm người dùng mới");
        
        if (isEditing) {
            etEmail.setText(userToEdit.getEmail());
            etEmail.setEnabled(false); // Don't allow email changes
            tilPassword.setVisibility(View.GONE); // Don't show password field when editing
            etFullName.setText(userToEdit.getFullName());
            etPhone.setText(userToEdit.getPhoneNumber());
            actvRole.setText(userToEdit.getRole(), false);
            etDescription.setText(userToEdit.getDescription());
            
            if ("EMPLOYER".equals(userToEdit.getRole())) {
                tilPostQuota.setVisibility(View.VISIBLE);
                etPostQuota.setText(String.valueOf(userToEdit.getPostQuota()));
            }
            
            if (userToEdit.getGender() != null) {
                if (userToEdit.getGender()) {
                    rgGender.check(R.id.rbMale);
                } else {
                    rgGender.check(R.id.rbFemale);
                }
            }
            
            cbVerified.setChecked(userToEdit.getIsVerified());
            cbActive.setChecked(userToEdit.getIsActive());
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            if (validateAndSaveUser(dialogView, userToEdit, isEditing)) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean validateAndSaveUser(View dialogView, UserEntity userToEdit, boolean isEditing) {
        // Get form data
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        AutoCompleteTextView actvRole = dialogView.findViewById(R.id.actvRole);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPostQuota = dialogView.findViewById(R.id.etPostQuota);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        CheckBox cbVerified = dialogView.findViewById(R.id.cbVerified);
        CheckBox cbActive = dialogView.findViewById(R.id.cbActive);

        // Validate required fields
        String email = etEmail.getText().toString().trim();
        String password = isEditing ? "" : etPassword.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String role = actvRole.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email không được để trống");
            return false;
        }

        if (!isEditing && password.isEmpty()) {
            etPassword.setError("Mật khẩu không được để trống");
            return false;
        }

        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên không được để trống");
            return false;
        }

        if (role.isEmpty()) {
            actvRole.setError("Vui lòng chọn vai trò");
            return false;
        }

        // Create or update user
        try {
            if (isEditing) {
                updateUser(userToEdit, dialogView);
            } else {
                createUser(dialogView);
            }
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void createUser(View dialogView) {
        TextInputEditText etEmail = dialogView.findViewById(R.id.etEmail);
        TextInputEditText etPassword = dialogView.findViewById(R.id.etPassword);
        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        AutoCompleteTextView actvRole = dialogView.findViewById(R.id.actvRole);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPostQuota = dialogView.findViewById(R.id.etPostQuota);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        CheckBox cbVerified = dialogView.findViewById(R.id.cbVerified);
        CheckBox cbActive = dialogView.findViewById(R.id.cbActive);

        User newUser = new User(
                etEmail.getText().toString().trim(),
                etPassword.getText().toString().trim(),
                etFullName.getText().toString().trim(),
                actvRole.getText().toString().trim()
        );

        // Set optional fields
        String phone = etPhone.getText().toString().trim();
        if (!phone.isEmpty()) {
            newUser.setPhoneNumber(phone);
        }

        String description = etDescription.getText().toString().trim();
        if (!description.isEmpty()) {
            newUser.setDescription(description);
        }

        // Gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rbMale) {
            newUser.setGender(true);
        } else if (selectedGenderId == R.id.rbFemale) {
            newUser.setGender(false);
        }

        // Post quota for employers
        if ("EMPLOYER".equals(newUser.getRole())) {
            String postQuotaStr = etPostQuota.getText().toString().trim();
            if (!postQuotaStr.isEmpty()) {
                newUser.setPostQuota(Integer.parseInt(postQuotaStr));
            } else {
                newUser.setPostQuota(50); // Default
            }
        }

        newUser.setVerified(cbVerified.isChecked());
        newUser.setIsActive(cbActive.isChecked());
        newUser.setCreatedAt(new Date());

        // Save to database
        executorService.execute(() -> {
            try {
                databaseHelper.insertUser(newUser);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Thêm người dùng thành công", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi thêm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUser(UserEntity userToEdit, View dialogView) {
        android.util.Log.d("AdminUserManagement", "Starting updateUser for: " + userToEdit.getEmail());
        
        // Get form data
        TextInputEditText etFullName = dialogView.findViewById(R.id.etFullName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        AutoCompleteTextView actvRole = dialogView.findViewById(R.id.actvRole);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        TextInputEditText etPostQuota = dialogView.findViewById(R.id.etPostQuota);
        RadioGroup rgGender = dialogView.findViewById(R.id.rgGender);
        CheckBox cbVerified = dialogView.findViewById(R.id.cbVerified);
        CheckBox cbActive = dialogView.findViewById(R.id.cbActive);

        // Update user data
        userToEdit.setFullName(etFullName.getText().toString().trim());
        userToEdit.setPhoneNumber(etPhone.getText().toString().trim());
        userToEdit.setRole(actvRole.getText().toString().trim());
        userToEdit.setDescription(etDescription.getText().toString().trim());
        
        // Handle gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rbMale) {
            userToEdit.setGender(true);
        } else if (selectedGenderId == R.id.rbFemale) {
            userToEdit.setGender(false);
        } else {
            userToEdit.setGender(null);
        }
        
        // Handle post quota for employers
        if ("EMPLOYER".equals(userToEdit.getRole())) {
            try {
                String quotaText = etPostQuota.getText().toString().trim();
                int quota = quotaText.isEmpty() ? 0 : Integer.parseInt(quotaText);
                userToEdit.setPostQuota(quota);
            } catch (NumberFormatException e) {
                userToEdit.setPostQuota(0);
            }
        } else {
            userToEdit.setPostQuota(null);
        }
        
        userToEdit.setIsVerified(cbVerified.isChecked());
        userToEdit.setIsActive(cbActive.isChecked());

        android.util.Log.d("AdminUserManagement", "=== USER DATA BEFORE UPDATE ===");
        android.util.Log.d("AdminUserManagement", "Email: " + userToEdit.getEmail());
        android.util.Log.d("AdminUserManagement", "Full Name: " + userToEdit.getFullName());
        android.util.Log.d("AdminUserManagement", "Description: " + userToEdit.getDescription());
        android.util.Log.d("AdminUserManagement", "Role: " + userToEdit.getRole());
        android.util.Log.d("AdminUserManagement", "Phone: " + userToEdit.getPhoneNumber());
        android.util.Log.d("AdminUserManagement", "ID: " + userToEdit.getId());
        android.util.Log.d("AdminUserManagement", "=========================================");

        // Update in database
        executorService.execute(() -> {
            try {
                android.util.Log.d("AdminUserManagement", "Calling databaseHelper.updateUser");
                boolean success = databaseHelper.updateUser(userToEdit);
                
                runOnUiThread(() -> {
                    if (success) {
                        android.util.Log.d("AdminUserManagement", "User update successful");
                        Toast.makeText(this, "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        android.util.Log.e("AdminUserManagement", "User update failed");
                        Toast.makeText(this, "Cập nhật người dùng thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AdminUserManagement", "Error updating user: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi cập nhật người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // UserManagementAdapter.OnUserActionListener implementation
    @Override
    public void onUserClick(UserEntity user) {
        // Handle user click - maybe show details
    }

    @Override
    public void onEditUser(UserEntity user) {
        showUserFormDialog(user);
    }

    @Override
    public void onDeleteUser(UserEntity user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa người dùng \"" + user.getFullName() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    executorService.execute(() -> {
                        try {
                            databaseHelper.deleteUser(user.getEmail());
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Lỗi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onToggleUserStatus(UserEntity user) {
        String action = user.getIsActive() ? "khóa" : "mở khóa";
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage("Bạn có chắc muốn " + action + " tài khoản \"" + user.getFullName() + "\"?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    android.util.Log.d("AdminUserManagement", "Toggling user status for: " + user.getEmail());
                    android.util.Log.d("AdminUserManagement", "Current status: " + user.getIsActive());
                    
                    // Toggle status
                    boolean newStatus = !user.getIsActive();
                    user.setIsActive(newStatus);
                    
                    android.util.Log.d("AdminUserManagement", "New status: " + newStatus);
                    
                    // Update in database
                    executorService.execute(() -> {
                        try {
                            boolean success = databaseHelper.updateUser(user);
                            
                            runOnUiThread(() -> {
                                if (success) {
                                    android.util.Log.d("AdminUserManagement", "User status toggle successful");
                                    String statusMessage = newStatus ? "mở khóa" : "khóa";
                                    Toast.makeText(this, "Đã " + statusMessage + " tài khoản thành công", Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                } else {
                                    android.util.Log.e("AdminUserManagement", "User status toggle failed");
                                    Toast.makeText(this, "Cập nhật trạng thái thất bại", Toast.LENGTH_SHORT).show();
                                    // Revert status if failed
                                    user.setIsActive(!newStatus);
                                }
                            });
                        } catch (Exception e) {
                            android.util.Log.e("AdminUserManagement", "Error toggling user status: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Lỗi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // Revert status if failed
                                user.setIsActive(!newStatus);
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onChangeUserPassword(UserEntity user) {
        showChangeUserPasswordDialog(user);
    }
    
    private void showChangeUserPasswordDialog(UserEntity user) {
        // Tạo layout cho dialog đổi mật khẩu
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Hiển thị thông tin user
        TextView tvUserInfo = new TextView(this);
        tvUserInfo.setText("Đổi mật khẩu cho: " + user.getFullName() + "\nEmail: " + user.getEmail());
        tvUserInfo.setTextSize(16);
        tvUserInfo.setPadding(0, 0, 0, 30);
        layout.addView(tvUserInfo);
        
        // Mật khẩu mới
        com.google.android.material.textfield.TextInputLayout tilNewPassword = new com.google.android.material.textfield.TextInputLayout(this);
        com.google.android.material.textfield.TextInputEditText etNewPassword = new com.google.android.material.textfield.TextInputEditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilNewPassword.addView(etNewPassword);
        
        // Xác nhận mật khẩu
        com.google.android.material.textfield.TextInputLayout tilConfirmPassword = new com.google.android.material.textfield.TextInputLayout(this);
        com.google.android.material.textfield.TextInputEditText etConfirmPassword = new com.google.android.material.textfield.TextInputEditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilConfirmPassword.addView(etConfirmPassword);
        
        layout.addView(tilNewPassword);
        layout.addView(tilConfirmPassword);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);
        builder.setTitle("🔒 Đổi mật khẩu cho " + user.getFullName());
        builder.setIcon(R.drawable.ic_person);
        
        builder.setPositiveButton("💾 Đổi mật khẩu", (dialog, which) -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            
            if (validateUserPasswordChange(newPassword, confirmPassword)) {
                changeUserPassword(user, newPassword);
            }
        });
        
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
    
    private boolean validateUserPasswordChange(String newPassword, String confirmPassword) {
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
    
    private void changeUserPassword(UserEntity user, String newPassword) {
        android.util.Log.d("AdminUserManagement", "=== CHANGE USER PASSWORD START ===");
        android.util.Log.d("AdminUserManagement", "User: " + user.getEmail() + " - " + user.getFullName());
        android.util.Log.d("AdminUserManagement", "New password length: " + newPassword.length());
        
        // Tạo LoginController để sử dụng method adminChangeUserPassword
        com.example.projectprm392.controllers.LoginController loginController = 
            new com.example.projectprm392.controllers.LoginController(this);
            
        loginController.adminChangeUserPassword(user.getEmail(), newPassword, 
            new com.example.projectprm392.controllers.LoginController.UpdatePasswordCallback() {
                @Override
                public void onSuccess() {
                    android.util.Log.d("AdminUserManagement", "Password change SUCCESS callback received");
                    android.util.Log.d("AdminUserManagement", "=== CHANGE USER PASSWORD END - SUCCESS ===");
                    runOnUiThread(() -> {
                        Toast.makeText(AdminUserManagementActivity.this, 
                            "✅ Đổi mật khẩu thành công cho " + user.getFullName(), 
                            Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    android.util.Log.e("AdminUserManagement", "Password change ERROR callback: " + error);
                    android.util.Log.d("AdminUserManagement", "=== CHANGE USER PASSWORD END - ERROR ===");
                    runOnUiThread(() -> {
                        Toast.makeText(AdminUserManagementActivity.this, 
                            "❌ Lỗi đổi mật khẩu: " + error, 
                            Toast.LENGTH_SHORT).show();
                    });
                }
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Xử lý nút back trên toolbar
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
