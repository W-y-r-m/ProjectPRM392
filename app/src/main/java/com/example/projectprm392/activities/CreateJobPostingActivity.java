package com.example.projectprm392.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class CreateJobPostingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private Toolbar toolbar;
    private EditText edtTitle;
    private EditText edtDescription;
    private EditText edtSalary;
    private EditText edtLocation;
    private AutoCompleteTextView spinnerJobType;
    private AutoCompleteTextView spinnerExperienceLevel;
    private EditText edtNeededAmount;
    private EditText edtWorkingTime;
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
            setContentView(R.layout.activity_create_job_posting);

            initViews();
            setupToolbar();
            setupSpinners();
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
        edtSalary = findViewById(R.id.edtSalary);
        edtLocation = findViewById(R.id.edtLocation);
        spinnerJobType = findViewById(R.id.spinnerJobType);
        spinnerExperienceLevel = findViewById(R.id.spinnerExperienceLevel);
        edtNeededAmount = findViewById(R.id.edtNeededAmount);
        edtWorkingTime = findViewById(R.id.edtWorkingTime);
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
            getSupportActionBar().setTitle("Đăng tin tuyển dụng");
        }
    }

    private void setupSpinners() {
        try {
            // Job Type Spinner
            String[] jobTypes = {"FULL_TIME", "PART_TIME", "FREELANCE", "INTERNSHIP"};
            ArrayAdapter<String> jobTypeAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, jobTypes);
            jobTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerJobType.setAdapter(jobTypeAdapter);
            spinnerJobType.setKeyListener(null);
            spinnerJobType.setOnClickListener(v -> spinnerJobType.showDropDown());
            
            // Experience Level Spinner
            String[] experienceLevels = {"ENTRY", "JUNIOR", "SENIOR", "EXPERIENCED"};
            ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, experienceLevels);
            experienceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerExperienceLevel.setAdapter(experienceAdapter);
            spinnerExperienceLevel.setKeyListener(null);
            spinnerExperienceLevel.setOnClickListener(v -> spinnerExperienceLevel.showDropDown());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi setup dropdown: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        String salary = edtSalary.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String jobType = spinnerJobType.getText().toString().trim();
        String experienceLevel = spinnerExperienceLevel.getText().toString().trim();
        String neededAmountStr = edtNeededAmount.getText().toString().trim();
        String workingTime = edtWorkingTime.getText().toString().trim();

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

        if (TextUtils.isEmpty(salary)) {
            edtSalary.setError("Vui lòng nhập mức lương");
            edtSalary.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(location)) {
            edtLocation.setError("Vui lòng nhập địa điểm");
            edtLocation.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(jobType)) {
            spinnerJobType.setError("Vui lòng chọn loại công việc");
            spinnerJobType.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(experienceLevel)) {
            spinnerExperienceLevel.setError("Vui lòng chọn cấp độ kinh nghiệm");
            spinnerExperienceLevel.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(neededAmountStr)) {
            edtNeededAmount.setError("Vui lòng nhập số lượng cần tuyển");
            edtNeededAmount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(workingTime)) {
            edtWorkingTime.setError("Vui lòng nhập thời gian làm việc");
            edtWorkingTime.requestFocus();
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
        checkQuotaAndSubmit(title, description, salary, location, jobType, 
                experienceLevel, neededAmount, workingTime);
    }

    private void checkQuotaAndSubmit(String title, String description, String salary, 
                                   String location, String jobType, String experienceLevel,
                                   int neededAmount, String workingTime) {
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

        // Create job posting
        createJobPosting(currentUser, title, description, salary, location, 
                jobType, experienceLevel, neededAmount, workingTime);
    }

    private void createJobPosting(UserEntity user, String title, String description, 
                                String salary, String location, String jobType, 
                                String experienceLevel, int neededAmount, String workingTime) {
        try {
            JobEntity jobEntity = new JobEntity();
            jobEntity.setJobId(UUID.randomUUID().toString());
            jobEntity.setUserId(user.getId());
            jobEntity.setTitle(title);
            jobEntity.setDescription(description);
            jobEntity.setSalary(salary);
            jobEntity.setLocation(location);
            jobEntity.setJobType(jobType);
            jobEntity.setExperienceLevel(experienceLevel);
            jobEntity.setNeededAmount(neededAmount);
            jobEntity.setWorkingTime(workingTime);
            jobEntity.setLocationLatitude(currentLocation.getLatitude());
            jobEntity.setLocationLongitude(currentLocation.getLongitude());
            jobEntity.setPostType("JOB_POSTING");
            jobEntity.setStatus("ACTIVE");
            jobEntity.setCreatedAt(new Date());
            jobEntity.setIsActive(true);

            // Insert job
            boolean success = databaseHelper.insertJob(jobEntity);

            if (success) {
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

        Toast.makeText(this, "Đăng tin tuyển dụng thành công!", Toast.LENGTH_SHORT).show();
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
