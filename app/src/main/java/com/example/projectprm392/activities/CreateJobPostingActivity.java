package com.example.projectprm392.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateJobPostingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_MAP_LOCATION = 1002;
    private static final String TAG = "CreateJobPostingActivity";

    private Toolbar toolbar;
    private EditText edtTitle;
    private EditText edtDescription;
    private EditText edtSalary;
    private AutoCompleteTextView spinnerJobType;
    private AutoCompleteTextView spinnerExperienceLevel;
    private EditText edtNeededAmount;
    private EditText edtStartDate;
    private EditText edtEndDate;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressIndicator;
    
    // Location selection views
    private CardView cardCurrentLocation;
    private CardView cardManualLocation;
    private CardView cardSelectedLocation;
    private TextView txtSelectedLocation;
    private TextView tvLocationStatus;

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLocation;
    private String selectedLocationName = "";
    private boolean isLocationSelected = false;
    private UserEntity currentUser;
    
    // Date fields
    private Date selectedStartDate;
    private Date selectedEndDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

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
        spinnerJobType = findViewById(R.id.spinnerJobType);
        spinnerExperienceLevel = findViewById(R.id.spinnerExperienceLevel);
        edtNeededAmount = findViewById(R.id.edtNeededAmount);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressIndicator = findViewById(R.id.progressIndicator);
        
        // Location selection views
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation);
        cardManualLocation = findViewById(R.id.cardManualLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);

        sessionManager = new SessionManager(this);
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Hide location input initially
        // Initialize location selection views
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation);
        cardManualLocation = findViewById(R.id.cardManualLocation);
        cardSelectedLocation = findViewById(R.id.cardSelectedLocation);
        txtSelectedLocation = findViewById(R.id.txtSelectedLocation);
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
        
        // Location selection listeners
        if (cardCurrentLocation != null) {
            cardCurrentLocation.setOnClickListener(v -> selectCurrentLocation());
        }
        
        if (cardManualLocation != null) {
            cardManualLocation.setOnClickListener(v -> selectManualLocation());
        }
        
        // Date picker listeners
        edtStartDate.setOnClickListener(v -> showStartDatePicker());
        edtEndDate.setOnClickListener(v -> showEndDatePicker());
    }

    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
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
                        selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Toast.makeText(this, "Đã lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void selectCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        isLocationSelected = true;
                        
                        // Get address from coordinates
                        getAddressFromLocation(selectedLocation);
                        
                        // Update UI
                        updateLocationUI();
                        Toast.makeText(this, "Đã chọn vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void selectManualLocation() {
        Intent intent = new Intent(this, com.example.projectprm392.activities.MapLocationPickerActivity.class);
        startActivityForResult(intent, REQUEST_MAP_LOCATION);
    }
    
    private void getAddressFromLocation(LatLng location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                location.latitude, location.longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                selectedLocationName = address.getAddressLine(0);
            } else {
                selectedLocationName = "Vị trí đã chọn";
            }
        } catch (IOException e) {
            selectedLocationName = "Vị trí đã chọn";
        }
    }
    
    private void updateLocationUI() {
        if (cardSelectedLocation != null && txtSelectedLocation != null) {
            cardSelectedLocation.setVisibility(View.VISIBLE);
            txtSelectedLocation.setText(selectedLocationName);
        }
    }

    private void validateAndSubmit() {
        Log.d(TAG, "=== Starting validateAndSubmit ===");
        
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String salary = edtSalary.getText().toString().trim();
        String jobType = spinnerJobType.getText().toString().trim();
        String experienceLevel = spinnerExperienceLevel.getText().toString().trim();
        String neededAmountStr = edtNeededAmount.getText().toString().trim();

        Log.d(TAG, "Form data - Title: " + title + ", Location selected: " + isLocationSelected + 
              ", JobType: " + jobType + ", Experience: " + experienceLevel);

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

        if (!isLocationSelected || selectedLocation == null) {
            Toast.makeText(this, "Vui lòng chọn vị trí làm việc", Toast.LENGTH_SHORT).show();
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

        if (selectedStartDate == null) {
            edtStartDate.setError("Vui lòng chọn ngày bắt đầu");
            edtStartDate.requestFocus();
            return;
        }

        if (selectedEndDate == null) {
            edtEndDate.setError("Vui lòng chọn ngày kết thúc");
            edtEndDate.requestFocus();
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

        if (!isLocationSelected || selectedLocation == null) {
            Toast.makeText(this, "Vui lòng chọn vị trí làm việc", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "All validation passed, proceeding to quota check");
        // Check user quota
        checkQuotaAndSubmit(title, description, salary, selectedLocationName, jobType, 
                experienceLevel, neededAmount, selectedStartDate, selectedEndDate);
    }

    private void checkQuotaAndSubmit(String title, String description, String salary, 
                                   String location, String jobType, String experienceLevel,
                                   int neededAmount, Date startDate, Date endDate) {
        Log.d(TAG, "=== Starting checkQuotaAndSubmit ===");
        progressIndicator.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Get current user
        String userEmail = sessionManager.getEmail();
        Log.d(TAG, "User email from session: " + userEmail);
        
        UserEntity currentUser = databaseHelper.getUserByEmail(userEmail);

        if (currentUser == null) {
            Log.e(TAG, "Cannot find user with email: " + userEmail);
            showError("Không thể lấy thông tin người dùng");
            return;
        }

        Log.d(TAG, "Found user: " + currentUser.getId() + ", current quota: " + currentUser.getPostQuota());

        // Check post quota
        if (currentUser.getPostQuota() >= 10) {
            Log.d(TAG, "User quota exceeded: " + currentUser.getPostQuota());
            showQuotaExceededDialog();
            return;
        }

        Log.d(TAG, "Quota check passed, creating job posting");
        // Create job posting
        createJobPosting(currentUser, title, description, salary, location, 
                jobType, experienceLevel, neededAmount, startDate, endDate);
    }

    private void createJobPosting(UserEntity user, String title, String description, 
                                String salary, String location, String jobType, 
                                String experienceLevel, int neededAmount, Date startDate, Date endDate) {
        try {
            Log.d(TAG, "Creating job posting for user: " + user.getId());
            
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
            jobEntity.setStartDate(startDate);
            jobEntity.setEndDate(endDate);
            jobEntity.setLocationLatitude(selectedLocation.latitude);
            jobEntity.setLocationLongitude(selectedLocation.longitude);
            jobEntity.setPostType("JOB_POSTING");
            jobEntity.setStatus("ACTIVE");
            jobEntity.setCreatedAt(new Date());
            jobEntity.setIsActive(true);

            Log.d(TAG, "Job entity created with title: " + title);
            Log.d(TAG, "Location: " + location + " (" + selectedLocation.latitude + ", " + selectedLocation.longitude + ")");
            Log.d(TAG, "Start date: " + dateFormat.format(startDate) + ", End date: " + dateFormat.format(endDate));

            // Insert job
            boolean success = databaseHelper.insertJob(jobEntity);
            Log.d(TAG, "Database insert result: " + success);

            if (success) {
                Log.d(TAG, "Job posted successfully, updating user quota");
                // Update user's post quota
                user.setPostQuota(user.getPostQuota() + 1);
                boolean userUpdateSuccess = databaseHelper.updateUser(databaseHelper.convertEntityToUser(user));
                Log.d(TAG, "User quota update result: " + userUpdateSuccess);

                showSuccess();
            } else {
                Log.e(TAG, "Failed to insert job into database");
                showError("Không thể tạo bài đăng - lỗi database");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception in createJobPosting: " + e.getMessage(), e);
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
                    // Navigate to payment activity
                    Intent paymentIntent = new Intent(this, com.example.projectprm392.activities.PaymentActivity.class);
                    startActivity(paymentIntent);
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

    private void showStartDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedStartDate != null) {
            calendar.setTime(selectedStartDate);
        }

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedStartDate = selectedCalendar.getTime();
                    edtStartDate.setText(dateFormat.format(selectedStartDate));
                    
                    // Validate that start date is not after end date
                    if (selectedEndDate != null && selectedStartDate.after(selectedEndDate)) {
                        Toast.makeText(this, "Ngày bắt đầu không thể sau ngày kết thúc", Toast.LENGTH_SHORT).show();
                        selectedStartDate = null;
                        edtStartDate.setText("");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedEndDate != null) {
            calendar.setTime(selectedEndDate);
        } else if (selectedStartDate != null) {
            calendar.setTime(selectedStartDate);
        }

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedEndDate = selectedCalendar.getTime();
                    edtEndDate.setText(dateFormat.format(selectedEndDate));
                    
                    // Validate that end date is not before start date
                    if (selectedStartDate != null && selectedEndDate.before(selectedStartDate)) {
                        Toast.makeText(this, "Ngày kết thúc không thể trước ngày bắt đầu", Toast.LENGTH_SHORT).show();
                        selectedEndDate = null;
                        edtEndDate.setText("");
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to start date or today
        long minDate = selectedStartDate != null ? selectedStartDate.getTime() : System.currentTimeMillis();
        datePickerDialog.getDatePicker().setMinDate(minDate);
        datePickerDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_MAP_LOCATION && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            
            if (latitude != 0 && longitude != 0) {
                selectedLocation = new LatLng(latitude, longitude);
                getAddressFromLocation(selectedLocation);
                Log.d(TAG, "Selected location from map: " + latitude + ", " + longitude);
                
                // Update UI
                updateLocationUI();
                isLocationSelected = true;
                Toast.makeText(this, "Đã chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Vị trí chọn không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
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
