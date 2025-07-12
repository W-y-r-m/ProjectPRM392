package com.example.projectprm392.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Date;
import java.util.UUID;

public class CreateJobSeekingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private Toolbar toolbar;
    private EditText edtTitle;
    private EditText edtDescription;
    private EditText edtNeededAmount;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressIndicator;

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_create_job_seeking);

            initViews();
            setupToolbar();
            setupListeners();
            checkPermissionAndGetLocation();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtNeededAmount = findViewById(R.id.edtNeededAmount);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressIndicator = findViewById(R.id.progressIndicator);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đăng tin tìm việc");
        }
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        Toast.makeText(this, "Đã lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void validateAndSubmit() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String neededAmountStr = edtNeededAmount.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Vui lòng nhập tiêu đề");
            edtTitle.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            edtDescription.setError("Vui lòng nhập mô tả");
            edtDescription.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(neededAmountStr)) {
            edtNeededAmount.setError("Vui lòng nhập số lượng người cần");
            edtNeededAmount.requestFocus();
            return;
        }

        int neededAmount;
        try {
            neededAmount = Integer.parseInt(neededAmountStr);
            if (neededAmount <= 0) {
                edtNeededAmount.setError("Số lượng phải lớn hơn 0");
                edtNeededAmount.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            edtNeededAmount.setError("Số lượng không hợp lệ");
            edtNeededAmount.requestFocus();
            return;
        }

        if (currentLocation == null) {
            Toast.makeText(this, "Đang lấy vị trí hiện tại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        // Check user quota
        checkQuotaAndSubmit(title, description, neededAmount);
    }

    private void checkQuotaAndSubmit(String title, String description, int neededAmount) {
        progressIndicator.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Get current user
        String userEmail = sessionManager.getEmail();
        UserEntity currentUser = databaseHelper.getUserByEmail(userEmail);

        if (currentUser == null) {
            showError("Không thể lấy thông tin người dùng");
            return;
        }

        // Check post quota
        if (currentUser.getPostQuota() >= 10) {
            showQuotaExceededDialog();
            return;
        }

        // Create job seeking post
        createJobSeekingPost(currentUser, title, description, neededAmount);
    }

    private void createJobSeekingPost(UserEntity user, String title, String description, int neededAmount) {
        try {
            JobEntity jobEntity = new JobEntity();
            jobEntity.setJobId(UUID.randomUUID().toString());
            jobEntity.setUserId(user.getId());
            jobEntity.setTitle(title);
            jobEntity.setDescription(description);
            jobEntity.setSalary("Thỏa thuận"); // Default for job seeking
            jobEntity.setLocation("Hà Nội"); // Could be improved by reverse geocoding
            jobEntity.setJobType("JOB_SEEKING");
            jobEntity.setExperienceLevel("ANY");
            jobEntity.setNeededAmount(neededAmount);
            jobEntity.setWorkingTime("Linh hoạt");
            jobEntity.setLocationLatitude(currentLocation.getLatitude());
            jobEntity.setLocationLongitude(currentLocation.getLongitude());
            jobEntity.setPostType("JOB_SEEKING");
            jobEntity.setStatus("ACTIVE");
            jobEntity.setCreatedAt(new Date());
            jobEntity.setIsActive(true);

            // Insert job
            long jobId = databaseHelper.insertJob(jobEntity);

            if (jobId > 0) {
                // Update user's post quota
                user.setPostQuota(user.getPostQuota() + 1);
                databaseHelper.updateUser(databaseHelper.convertEntityToUser(user));

                showSuccess();
            } else {
                showError("Không thể tạo bài đăng");
            }

        } catch (Exception e) {
            showError("Lỗi khi tạo bài đăng: " + e.getMessage());
        }
    }

    private void showQuotaExceededDialog() {
        progressIndicator.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Hết lượt đăng tin")
                .setMessage("Bạn đã hết số lượng khuyến mãi đăng tin (10 tin). Vui lòng nạp thêm tiền để đăng tin.")
                .setPositiveButton("Nạp tiền", (dialog, which) -> {
                    // TODO: Navigate to payment activity
                    Toast.makeText(this, "Tính năng nạp tiền đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showSuccess() {
        progressIndicator.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);

        Toast.makeText(this, "Đăng tin tìm việc thành công!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showError(String message) {
        progressIndicator.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Cần quyền truy cập vị trí để đăng tin", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
