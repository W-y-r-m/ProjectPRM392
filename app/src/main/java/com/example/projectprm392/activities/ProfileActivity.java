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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    
    private EditText etFullName, etPhoneNumber, etDescription;
    private EditText etEmail, etPostQuota;
    private Spinner spGender;
    private Button btnSave, btnCancel, btnCurrentLocation;
    private ImageView ivBack;
    private GoogleMap mMap;
    private LatLng selectedLocation;
    
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
        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        ivBack = findViewById(R.id.ivBack);
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
        
        btnSave.setOnClickListener(v -> saveProfile());
        
        btnCurrentLocation.setOnClickListener(v -> getCurrentLocation());
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
        
        // Set location on map
        if (user.getCurrentLatitude() != null && user.getCurrentLongitude() != null) {
            selectedLocation = new LatLng(user.getCurrentLatitude(), user.getCurrentLongitude());
            if (mMap != null) {
                updateMapLocation();
            }
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

    private void getAddressFromLocation(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                Toast.makeText(this, "Địa chỉ: " + addressText, Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            currentUser.setCurrentLatitude(selectedLocation.latitude);
            currentUser.setCurrentLongitude(selectedLocation.longitude);
        }

        // Save to database
        userController.updateUser(currentUser, new UserController.UserCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Update session manager
                    sessionManager.saveUserSession(
                            user.getUserId().toString(),
                            user.getEmail(),
                            user.getFullName(),
                            user.getRole(),
                            sessionManager.getToken()
                    );
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
