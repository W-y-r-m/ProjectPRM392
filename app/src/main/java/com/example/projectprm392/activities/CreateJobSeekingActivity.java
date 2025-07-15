package com.example.projectprm392.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CreateJobSeekingActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int MAP_SELECTION_REQUEST = 1002;

    private Toolbar toolbar;
    private EditText edtTitle;
    private EditText edtDescription;
    private CardView cardCurrentLocation;
    private CardView cardManualLocation;
    private TextView tvLocationStatus;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressIndicator;

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private Location selectedLocation;
    private String selectedLocationName = "";
    private boolean isLocationSelected = false;
    private UserEntity currentUser; // Add currentUser as field

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
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation);
        cardManualLocation = findViewById(R.id.cardManualLocation);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
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
        
        cardCurrentLocation.setOnClickListener(v -> useCurrentLocation());
        cardManualLocation.setOnClickListener(v -> openMapLocationPicker());
    }

    private void checkPermissionAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 
                    LOCATION_PERMISSION_REQUEST);
        } else {
            tvLocationStatus.setText("Nhấn để lấy vị trí hiện tại");
            // Tự động sử dụng vị trí mặc định ở Việt Nam để đảm bảo có vị trí
            useDefaultVietnamLocation();
        }
    }

    private void useCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        tvLocationStatus.setText("Đang lấy vị trí hiện tại...");
        tvLocationStatus.setTextColor(getColor(android.R.color.darker_gray));

        // Try to get current location instead of last known location
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Check if location is recent (within 5 minutes)
                            long locationAge = System.currentTimeMillis() - location.getTime();
                            if (locationAge < 5 * 60 * 1000) { // 5 minutes
                                // Log location for debugging
                                android.util.Log.d("LocationDebug", "Using cached location - Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                                
                                selectedLocation = location;
                                isLocationSelected = true;
                                getLocationName(location);
                            } else {
                                // Location is too old, get fresh location
                                android.util.Log.d("LocationDebug", "Location too old, requesting fresh location");
                                requestFreshLocation();
                            }
                        } else {
                            // If last location is null, request a fresh location update
                            android.util.Log.d("LocationDebug", "No cached location, requesting fresh location");
                            requestFreshLocation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("LocationDebug", "Failed to get last location: " + e.getMessage());
                        // Fallback to default Vietnam location
                        useDefaultVietnamLocation();
                    });
        } catch (SecurityException e) {
            android.util.Log.e("LocationDebug", "Security exception: " + e.getMessage());
            tvLocationStatus.setText("Không có quyền truy cập vị trí");
            tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED && 
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            android.util.Log.d("LocationDebug", "Requesting fresh location...");
            
            com.google.android.gms.location.LocationRequest locationRequest = 
                com.google.android.gms.location.LocationRequest.create()
                    .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1)
                    .setInterval(0)
                    .setFastestInterval(0);

            com.google.android.gms.location.LocationCallback locationCallback = 
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                        if (locationResult != null && locationResult.getLastLocation() != null) {
                            Location location = locationResult.getLastLocation();
                            
                            // Log fresh location for debugging
                            android.util.Log.d("LocationDebug", "Fresh Location - Latitude: " + location.getLatitude() + ", Longitude: " + location.getLongitude());
                            android.util.Log.d("LocationDebug", "Location accuracy: " + location.getAccuracy() + " meters");
                            android.util.Log.d("LocationDebug", "Location provider: " + location.getProvider());
                            
                            selectedLocation = location;
                            isLocationSelected = true;
                            getLocationName(location);
                            
                            // Stop location updates
                            fusedLocationClient.removeLocationUpdates(this);
                        } else {
                            android.util.Log.d("LocationDebug", "Fresh location result is null");
                            tvLocationStatus.setText("Không thể lấy vị trí hiện tại");
                            tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                        }
                    }
                    
                    @Override
                    public void onLocationAvailability(com.google.android.gms.location.LocationAvailability locationAvailability) {
                        android.util.Log.d("LocationDebug", "Location availability: " + locationAvailability.isLocationAvailable());
                        if (!locationAvailability.isLocationAvailable()) {
                            tvLocationStatus.setText("GPS không khả dụng, vui lòng bật GPS");
                            tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                        }
                    }
                };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            
            // Set a timeout to stop trying after 30 seconds
            new android.os.Handler().postDelayed(() -> {
                fusedLocationClient.removeLocationUpdates(locationCallback);
                if (!isLocationSelected) {
                    android.util.Log.d("LocationDebug", "Location request timeout, using default Vietnam location");
                    useDefaultVietnamLocation();
                }
            }, 30000);
            
        } catch (SecurityException e) {
            android.util.Log.e("LocationDebug", "Security exception in requestFreshLocation: " + e.getMessage());
            useDefaultVietnamLocation();
        }
    }

    /**
     * Sử dụng vị trí mặc định ở Việt Nam khi không thể lấy vị trí GPS
     */
    private void useDefaultVietnamLocation() {
        android.util.Log.d("LocationDebug", "Using default Vietnam location (Hanoi)");
        
        LocationUtils.LocationData defaultLocation = LocationUtils.getDefaultVietnamLocation();
        
        // Tạo Location object từ tọa độ mặc định
        selectedLocation = new Location("default");
        selectedLocation.setLatitude(defaultLocation.latitude);
        selectedLocation.setLongitude(defaultLocation.longitude);
        selectedLocation.setTime(System.currentTimeMillis());
        
        selectedLocationName = defaultLocation.address + " (vị trí mặc định)";
        isLocationSelected = true;
        
        // Cập nhật UI
        tvLocationStatus.setText("📍 " + selectedLocationName);
        tvLocationStatus.setTextColor(getColor(android.R.color.holo_blue_dark));
        
        Toast.makeText(this, "Đã sử dụng vị trí mặc định: " + defaultLocation.address, Toast.LENGTH_LONG).show();
    }

    /**
     * Mở MapLocationPickerActivity để người dùng chọn vị trí trên bản đồ
     */
    private void openMapLocationPicker() {
        Intent intent = new Intent(this, MapLocationPickerActivity.class);
        startActivityForResult(intent, MAP_SELECTION_REQUEST);
    }

    private void getLocationName(Location location) {
        try {
            android.util.Log.d("LocationDebug", "Getting location name for: " + location.getLatitude() + ", " + location.getLongitude());
            
            // Sử dụng LocationUtils để lấy địa chỉ với locale Việt Nam
            selectedLocationName = LocationUtils.getAddressFromCoordinates(this, 
                    location.getLatitude(), 
                    location.getLongitude());
            
            android.util.Log.d("LocationDebug", "Final address: " + selectedLocationName);
            
        } catch (Exception e) {
            selectedLocationName = "Vị trí hiện tại (lỗi không xác định)";
            android.util.Log.e("LocationDebug", "Unexpected error: " + e.getMessage());
        }
        
        // Cập nhật UI
        tvLocationStatus.setText("✓ " + selectedLocationName);
        tvLocationStatus.setTextColor(getColor(android.R.color.holo_green_dark));
    }

    private void validateAndSubmit() {
        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

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

        if (!isLocationSelected || selectedLocation == null) {
            Toast.makeText(this, "Vui lòng chọn vị trí làm việc", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check user quota
        checkQuotaAndSubmit(title, description);
    }

    private void checkQuotaAndSubmit(String title, String description) {
        progressIndicator.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Get current user
        String userEmail = sessionManager.getEmail();
        currentUser = databaseHelper.getUserByEmail(userEmail);

        if (currentUser == null) {
            showError("Không thể lấy thông tin người dùng");
            return;
        }

        // Check post quota (logic: quota tăng dần từ 0, tối đa 20 tin miễn phí)
        if (currentUser.getPostQuota() >= 20) {
            showQuotaExceededDialog();
            return;
        }

        // Create job seeking post (neededAmount = 1 for job seeking)
        createJobSeekingPost(currentUser, title, description, 1);
    }

    private void createJobSeekingPost(UserEntity user, String title, String description, int neededAmount) {
        try {
            android.util.Log.d("CreateJobSeeking", "=== CREATING JOB SEEKING POST ===");
            android.util.Log.d("CreateJobSeeking", "User ID: " + user.getId());
            android.util.Log.d("CreateJobSeeking", "Title: " + title);
            android.util.Log.d("CreateJobSeeking", "Description: " + description);
            android.util.Log.d("CreateJobSeeking", "Location: " + selectedLocationName);
            
            JobEntity jobEntity = new JobEntity();
            jobEntity.setJobId(UUID.randomUUID().toString());
            jobEntity.setUserId(user.getId());
            jobEntity.setTitle(title);
            jobEntity.setDescription(description);
            jobEntity.setSalary("Thỏa thuận"); // Default for job seeking
            jobEntity.setLocation(selectedLocationName.isEmpty() ? "Vị trí đã chọn" : selectedLocationName);
            jobEntity.setJobType("JOB_SEEKING");
            jobEntity.setExperienceLevel("ANY");
            jobEntity.setNeededAmount(neededAmount);
            jobEntity.setWorkingTime("Linh hoạt");
            jobEntity.setLocationLatitude(selectedLocation.getLatitude());
            jobEntity.setLocationLongitude(selectedLocation.getLongitude());
            jobEntity.setPostType("JOB_SEEKING");
            jobEntity.setStatus("ACTIVE");
            jobEntity.setCreatedAt(new Date());
            jobEntity.setIsActive(true);

            android.util.Log.d("CreateJobSeeking", "Generated Job ID: " + jobEntity.getJobId());
            android.util.Log.d("CreateJobSeeking", "Job Active: " + jobEntity.getIsActive());

            // Insert job
            boolean success = databaseHelper.insertJob(jobEntity);

            if (success) {
                // Update user's post quota - tăng 1 khi đăng tin thành công
                user.setPostQuota(user.getPostQuota() + 1);
                boolean userUpdated = databaseHelper.updateUser(databaseHelper.convertEntityToUser(user));
                
                android.util.Log.d("CreateJobSeeking", "Job created successfully: " + jobEntity.getJobId());
                android.util.Log.d("CreateJobSeeking", "Updated user quota: " + user.getPostQuota() + ", update result: " + userUpdated);

                showSuccess();
            } else {
                android.util.Log.e("CreateJobSeeking", "Failed to insert job into database");
                showError("Không thể tạo bài đăng");
            }

        } catch (Exception e) {
            android.util.Log.e("CreateJobSeeking", "Exception creating job seeking post", e);
            showError("Lỗi khi tạo bài đăng: " + e.getMessage());
        }
    }

    private void showQuotaExceededDialog() {
        progressIndicator.setVisibility(View.GONE);
        btnSubmit.setEnabled(true);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        AlertDialog show = builder.setTitle("Hết lượt đăng tin")
                .setMessage("Bạn đã hết số lượng khuyến mãi đăng tin (đã đăng " +
                        (currentUser.getPostQuota() != null ? currentUser.getPostQuota() : 0) +
                        "/20 tin). Vui lòng nạp tiền để tiếp tục đăng tin.\n\n💰 100,000đ sẽ giúp bạn có thêm 10 lượt đăng tin nữa")
                .setPositiveButton("Nạp tiền ngay", (dialog, which) -> {
                    openPaymentActivity();
                })
                .setNegativeButton("Để sau", null)
                .setCancelable(false)
                .show();
    }
    
    private void openPaymentActivity() {
        try {
            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            paymentIntent.putExtra(PaymentActivity.EXTRA_USER_ID, currentUser.getId());
            paymentIntent.putExtra(PaymentActivity.EXTRA_AMOUNT, 100000); // 100k VND
            paymentIntent.putExtra(PaymentActivity.EXTRA_DESCRIPTION, "Nạp tiền mua 10 lượt đăng tin việc làm");
            startActivity(paymentIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi mở trang thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
            boolean fineLocationGranted = false;
            boolean coarseLocationGranted = false;
            
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) && 
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    fineLocationGranted = true;
                }
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION) && 
                    grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    coarseLocationGranted = true;
                }
            }
            
            if (fineLocationGranted || coarseLocationGranted) {
                tvLocationStatus.setText("Nhấn để lấy vị trí hiện tại");
                // Tự động sử dụng vị trí mặc định để đảm bảo có vị trí
                useDefaultVietnamLocation();
            } else {
                // Nếu không có quyền GPS, vẫn sử dụng vị trí mặc định
                useDefaultVietnamLocation();
                Toast.makeText(this, "Sử dụng vị trí mặc định vì không có quyền GPS", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == MAP_SELECTION_REQUEST && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra(MapLocationPickerActivity.EXTRA_LATITUDE, 0);
            double longitude = data.getDoubleExtra(MapLocationPickerActivity.EXTRA_LONGITUDE, 0);
            String address = data.getStringExtra(MapLocationPickerActivity.EXTRA_ADDRESS);
            
            // Tạo Location object từ tọa độ được chọn
            selectedLocation = new Location("map_selected");
            selectedLocation.setLatitude(latitude);
            selectedLocation.setLongitude(longitude);
            selectedLocation.setTime(System.currentTimeMillis());
            
            selectedLocationName = address;
            isLocationSelected = true;
            
            // Cập nhật UI
            tvLocationStatus.setText("🗺️ " + selectedLocationName);
            tvLocationStatus.setTextColor(getColor(android.R.color.holo_blue_dark));
            
            Toast.makeText(this, "Đã chọn vị trí từ bản đồ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
