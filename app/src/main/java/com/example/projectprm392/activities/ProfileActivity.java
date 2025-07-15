package com.example.projectprm392.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.controllers.UserController;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_MAP_LOCATION = 1002;
    private static final String TAG = "ProfileActivity";
    
    private EditText etFullName, etPhoneNumber, etDescription;
    private EditText etEmail, etPostQuota;
    private Spinner spGender;
    private Button btnSave, btnCancel;
    private ImageView ivBack;
    private GoogleMap mMap;
    
    // Location selection views
    private CardView cardCurrentLocation;
    private CardView cardManualLocation;
    private CardView cardSelectedLocation;
    private TextView txtSelectedLocation;
    private TextView txtCurrentAddress;
    
    private LatLng selectedLocation;
    private String selectedLocationName = "";
    private String selectedAddress = "";
    private boolean isLocationSelected = false;
    
    private SessionManager sessionManager;
    private UserController userController;
    private User currentUser;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initViews();
        setupController();
        setupSpinner();
        setupListeners();
        loadUserData();
        initMap();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDescription = findViewById(R.id.etDescription);
        etEmail = findViewById(R.id.etEmail);
        etPostQuota = findViewById(R.id.etPostQuota);
        spGender = findViewById(R.id.spGender);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        ivBack = findViewById(R.id.ivBack);
        
        // Location selection views
        cardCurrentLocation = findViewById(R.id.cardCurrentLocation);
        cardManualLocation = findViewById(R.id.cardManualLocation);
        cardSelectedLocation = findViewById(R.id.cardSelectedLocation);
        txtSelectedLocation = findViewById(R.id.txtSelectedLocation);
        txtCurrentAddress = findViewById(R.id.txtCurrentAddress);
    }

    private void setupController() {
        sessionManager = new SessionManager(this);
        userController = new UserController(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(adapter);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        btnCancel.setOnClickListener(v -> finish());
        
        // Location selection listeners
        if (cardCurrentLocation != null) {
            cardCurrentLocation.setOnClickListener(v -> selectCurrentLocation());
        }
        
        if (cardManualLocation != null) {
            cardManualLocation.setOnClickListener(v -> selectManualLocation());
        }
        
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        String userId = sessionManager.getUserId();
        if (userId != null) {
            userController.getUserById(userId, new UserController.UserCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    runOnUiThread(() -> populateFields(user));
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> 
                        Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin: " + error, 
                                     Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }

    private void populateFields(User user) {
        etFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        etPhoneNumber.setText(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        etDescription.setText(user.getDescription() != null ? user.getDescription() : "");
        etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        etPostQuota.setText(String.valueOf(user.getPostQuota() != null ? user.getPostQuota() : 0));
        
        // Set gender
        if (user.getGender() != null) {
            spGender.setSelection(user.getGender() ? 0 : 1); // 0 = Nam, 1 = Nữ
        }
        
        // Set location on map and display current address
        if (user.getCurrentLatitude() != null && user.getCurrentLongitude() != null) {
            selectedLocation = new LatLng(user.getCurrentLatitude(), user.getCurrentLongitude());
            selectedAddress = user.getAddress() != null ? user.getAddress() : ""; // Load existing address
            
            if (mMap != null) {
                updateMapLocation();
            }
            
            // If we have stored address, use it, otherwise geocode
            if (selectedAddress != null && !selectedAddress.isEmpty()) {
                txtCurrentAddress.setText(selectedAddress);
            } else {
                // Load and display current address
                loadCurrentAddress(selectedLocation);
            }
        } else {
            txtCurrentAddress.setText("Chưa có địa chỉ");
            selectedAddress = "";
        }
        
        // Make non-editable fields readonly
        etEmail.setEnabled(false);
        etPostQuota.setEnabled(false);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set default location (Vietnam)
        LatLng defaultLocation = new LatLng(10.8231, 106.6297); // Ho Chi Minh City
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        
        // Set map click listener
        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            updateMapLocation();
            getAddressFromLocation(latLng);
        });
        
        // Update location if user data is already loaded
        if (selectedLocation != null) {
            updateMapLocation();
        }
    }

    private void updateMapLocation() {
        if (mMap != null && selectedLocation != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(selectedLocation)
                    .title("Vị trí của bạn"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation, 15));
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        selectedLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapLocation();
                        getAddressFromLocation(selectedLocation);
                    } else {
                        Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    }
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
                selectedAddress = selectedLocationName; // Store the address
                
                // Update UI to show selected location
                cardSelectedLocation.setVisibility(View.VISIBLE);
                txtSelectedLocation.setText(selectedLocationName);
                
                // Also update current address display
                txtCurrentAddress.setText(selectedLocationName);
            } else {
                selectedLocationName = "Vị trí đã chọn";
                selectedAddress = "";
                cardSelectedLocation.setVisibility(View.VISIBLE);
                txtSelectedLocation.setText(selectedLocationName);
                txtCurrentAddress.setText(selectedLocationName);
            }
        } catch (IOException e) {
            selectedLocationName = "Vị trí đã chọn";
            cardSelectedLocation.setVisibility(View.VISIBLE);
            txtSelectedLocation.setText(selectedLocationName);
            txtCurrentAddress.setText(selectedLocationName);
        }
    }
    
    private void updateLocationUI() {
        if (cardSelectedLocation != null && txtSelectedLocation != null) {
            cardSelectedLocation.setVisibility(View.VISIBLE);
            txtSelectedLocation.setText(selectedLocationName);
        }
    }

    private void loadCurrentAddress(LatLng location) {
        if (location == null) {
            txtCurrentAddress.setText("Chưa có địa chỉ");
            return;
        }
        
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                location.latitude, location.longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                txtCurrentAddress.setText(addressText);
                selectedAddress = addressText; // Store the address
            } else {
                txtCurrentAddress.setText("Không thể xác định địa chỉ");
                selectedAddress = "";
            }
        } catch (IOException e) {
            txtCurrentAddress.setText("Lỗi lấy địa chỉ");
            android.util.Log.e("ProfileActivity", "Error loading address: " + e.getMessage());
        }
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

    private void saveProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate input
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Họ tên không được để trống");
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Số điện thoại không được để trống");
            return;
        }

        // Update user object
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setDescription(description);
        currentUser.setGender(spGender.getSelectedItemPosition() == 0); // 0 = Nam, 1 = Nữ
        
        if (selectedLocation != null) {
            android.util.Log.d("ProfileActivity", "SAVING USER LOCATION:");
            android.util.Log.d("ProfileActivity", "Latitude: " + selectedLocation.latitude);
            android.util.Log.d("ProfileActivity", "Longitude: " + selectedLocation.longitude);
            android.util.Log.d("ProfileActivity", "Address: " + selectedAddress);
            
            currentUser.setCurrentLatitude(selectedLocation.latitude);
            currentUser.setCurrentLongitude(selectedLocation.longitude);
            currentUser.setAddress(selectedAddress); // Save the address text
        }

        // Save to database
        userController.updateUser(currentUser, new UserController.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Update session manager với thông tin mới
                    sessionManager.saveUserSession(
                            user.getUserId().toString(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getRole(),
                            sessionManager.getToken()
                    );
                    
                    // Đặt result để báo cho parent activity biết đã update thành công
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> 
                    Toast.makeText(ProfileActivity.this, "Lỗi cập nhật: " + error, 
                                 Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Cần cấp quyền vị trí để sử dụng chức năng này", 
                             Toast.LENGTH_SHORT).show();
            }
        }
    }
}
