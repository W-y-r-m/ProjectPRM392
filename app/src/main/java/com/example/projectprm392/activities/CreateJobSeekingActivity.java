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
    private TextView tvLocationStatus;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressIndicator;

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;
    private Location selectedLocation;
    private String selectedLocationName = "";
    private boolean isLocationSelected = false;

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
                        tvLocationStatus.setText("Lỗi khi lấy vị trí: " + e.getMessage());
                        tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
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
                    tvLocationStatus.setText("Timeout - không thể lấy vị trí");
                    tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
                }
            }, 30000);
            
        } catch (SecurityException e) {
            android.util.Log.e("LocationDebug", "Security exception in requestFreshLocation: " + e.getMessage());
            tvLocationStatus.setText("Không có quyền truy cập vị trí");
            tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }

    private void getLocationName(Location location) {
        try {
            // Use Vietnamese locale for better address parsing
            Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), 
                    location.getLongitude(), 
                    1
            );
            
            android.util.Log.d("LocationDebug", "Geocoder result count: " + (addresses != null ? addresses.size() : 0));
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                // Log all address components for debugging
                android.util.Log.d("LocationDebug", "Country: " + address.getCountryName());
                android.util.Log.d("LocationDebug", "AdminArea: " + address.getAdminArea());
                android.util.Log.d("LocationDebug", "Locality: " + address.getLocality());
                android.util.Log.d("LocationDebug", "SubLocality: " + address.getSubLocality());
                android.util.Log.d("LocationDebug", "Thoroughfare: " + address.getThoroughfare());
                android.util.Log.d("LocationDebug", "AddressLine(0): " + address.getAddressLine(0));
                
                // Build Vietnamese address format: Xã/Phường, Huyện, Tỉnh, Quốc gia
                StringBuilder locationBuilder = new StringBuilder();
                
                // Thêm xã/phường (SubLocality hoặc Locality)
                if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
                    locationBuilder.append(address.getSubLocality());
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    locationBuilder.append(address.getLocality());
                }
                
                // Thêm huyện/quận (từ AddressLine hoặc Locality)
                if (address.getLocality() != null && !address.getLocality().isEmpty() && 
                    !address.getLocality().equals(address.getSubLocality())) {
                    if (locationBuilder.length() > 0) locationBuilder.append(", ");
                    locationBuilder.append(address.getLocality());
                }
                
                // Thêm tỉnh/thành phố (AdminArea)
                if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                    if (locationBuilder.length() > 0) locationBuilder.append(", ");
                    locationBuilder.append(address.getAdminArea());
                }
                
                // Thêm quốc gia
                if (address.getCountryName() != null && !address.getCountryName().isEmpty()) {
                    if (locationBuilder.length() > 0) locationBuilder.append(", ");
                    locationBuilder.append(address.getCountryName());
                }
                
                selectedLocationName = locationBuilder.toString();
                
                // Nếu không build được địa chỉ từ các component, dùng address line
                if (selectedLocationName.trim().isEmpty()) {
                    selectedLocationName = address.getAddressLine(0);
                }
                
                // Fallback cuối cùng
                if (selectedLocationName == null || selectedLocationName.trim().isEmpty()) {
                    selectedLocationName = "Vị trí hiện tại";
                }
                
                android.util.Log.d("LocationDebug", "Final address: " + selectedLocationName);
                
            } else {
                selectedLocationName = "Vị trí hiện tại (không thể xác định địa chỉ)";
                android.util.Log.d("LocationDebug", "No addresses found");
            }
        } catch (IOException e) {
            selectedLocationName = "Vị trí hiện tại (lỗi geocoding)";
            android.util.Log.e("LocationDebug", "Geocoding error: " + e.getMessage());
        } catch (Exception e) {
            selectedLocationName = "Vị trí hiện tại (lỗi không xác định)";
            android.util.Log.e("LocationDebug", "Unexpected error: " + e.getMessage());
        }
        
        // Cập nhật UI
        tvLocationStatus.setText("✓ Đã lấy vị trí thành công");
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

        // Create job seeking post (neededAmount = 1 for job seeking)
        createJobSeekingPost(currentUser, title, description, 1);
    }

    private void createJobSeekingPost(UserEntity user, String title, String description, int neededAmount) {
        try {
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
            } else {
                tvLocationStatus.setText("Cần quyền truy cập vị trí để sử dụng tính năng này");
                tvLocationStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
