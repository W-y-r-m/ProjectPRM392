package com.example.projectprm392.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.utils.LocationUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapLocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapLocationPicker";
    
    // Intent keys
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_ADDRESS = "address";
    
    private GoogleMap mMap;
    private Toolbar toolbar;
    private EditText etSearch;
    private MaterialButton btnSearch;
    private ChipGroup chipGroupSuggestions;
    private TextView tvSelectedLocation;
    private MaterialButton btnConfirm;
    private FloatingActionButton fabMyLocation;
    
    private LatLng selectedLocation;
    private String selectedAddress = "";
    
    // Vị trí mặc định tại Hà Nội, Việt Nam
    private static final LatLng DEFAULT_VIETNAM_LOCATION = new LatLng(21.0285, 105.8542);
    private static final String DEFAULT_VIETNAM_ADDRESS = "Hà Nội, Việt Nam";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location_picker);

        initViews();
        setupToolbar();
        setupListeners();
        setupSearchSuggestions();
        initMap();
        
        // Set default location
        selectedLocation = DEFAULT_VIETNAM_LOCATION;
        selectedAddress = DEFAULT_VIETNAM_ADDRESS;
        updateLocationDisplay();
        
        // Khởi tạo nút xác nhận với vị trí mặc định ở Việt Nam
        updateConfirmButton(true);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        chipGroupSuggestions = findViewById(R.id.chipGroupSuggestions);
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation);
        btnConfirm = findViewById(R.id.btnConfirm);
        fabMyLocation = findViewById(R.id.fabMyLocation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn vị trí trên bản đồ");
        }
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        
        btnConfirm.setOnClickListener(v -> confirmLocation());
        
        // Search button listener
        btnSearch.setOnClickListener(v -> performSearch());
        
        // Search EditText listener cho phím Enter
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
        
        // Setup chip suggestions listeners
        setupChipSuggestions();
        
        fabMyLocation.setOnClickListener(v -> {
            if (mMap != null) {
                // Try to get current location if permission is granted
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    
                    // Try to get actual current location
                    FusedLocationProviderClient fusedLocationClient = 
                        LocationServices.getFusedLocationProviderClient(this);
                    
                    fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                                updateLocationFromLatLng(currentLocation);
                            } else {
                                // Fallback to Vietnam default
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_VIETNAM_LOCATION, 10f));
                                updateLocationFromLatLng(DEFAULT_VIETNAM_LOCATION);
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Fallback to Vietnam default
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_VIETNAM_LOCATION, 10f));
                            updateLocationFromLatLng(DEFAULT_VIETNAM_LOCATION);
                        });
                } else {
                    // No permission, use Vietnam default
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_VIETNAM_LOCATION, 10f));
                    updateLocationFromLatLng(DEFAULT_VIETNAM_LOCATION);
                }
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set map to focus on Vietnam by default but allow global selection
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_VIETNAM_LOCATION, 6f));
        
        // Add marker at default location
        mMap.addMarker(new MarkerOptions()
                .position(selectedLocation)
                .title("Vị trí được chọn")
                .snippet(selectedAddress));
        
        // Set up map click listener - ALLOW GLOBAL SELECTION
        mMap.setOnMapClickListener(latLng -> {
            Log.d(TAG, "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);
            updateLocationFromLatLng(latLng);
        });
        
        // Enable ALL map controls for better user experience
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false); // We use our custom button
        mMap.getUiSettings().setMapToolbarEnabled(true);
        
        // Set map type
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Set minimum and maximum zoom levels for better control
        mMap.setMinZoomPreference(2.0f);  // World view
        mMap.setMaxZoomPreference(21.0f); // Street level
        
        // Vẽ khung viền Việt Nam để người dùng biết phạm vi được phép
        drawVietnamBoundary();
    }
    
    /**
     * Vẽ khung viền chính xác hơn cho Việt Nam
     */
    private void drawVietnamBoundary() {
        // Tạo khung viền Việt Nam với các điểm chính xác hơn
        PolygonOptions vietnamBoundary = new PolygonOptions()
                // Bắc (biên giới Trung Quốc)
                .add(new LatLng(23.393, 105.417))  // Điểm cực bắc Hà Giang
                .add(new LatLng(23.393, 106.617))  
                .add(new LatLng(22.8, 107.8))      
                .add(new LatLng(21.5, 108.05))     // Quảng Ninh
                
                // Đông (biển Đông)
                .add(new LatLng(20.0, 107.3))      
                .add(new LatLng(18.0, 107.0))      
                .add(new LatLng(16.0, 108.22))     // Đà Nẵng
                .add(new LatLng(14.0, 109.0))      
                .add(new LatLng(12.0, 109.5))      
                .add(new LatLng(10.5, 109.2))      
                .add(new LatLng(8.6, 106.0))       // Cà Mau
                
                // Nam (biển)
                .add(new LatLng(8.38, 104.75))     // Điểm cực nam
                
                // Tây (biên giới Campuchia, Lào)
                .add(new LatLng(9.0, 104.0))       
                .add(new LatLng(10.5, 103.5))      
                .add(new LatLng(12.0, 102.8))      
                .add(new LatLng(14.0, 102.1))      
                .add(new LatLng(16.0, 102.8))      
                .add(new LatLng(18.0, 103.5))      
                .add(new LatLng(20.0, 104.0))      
                .add(new LatLng(22.0, 103.7))      
                
                .strokeColor(0xFF10B981)           // Màu xanh lá đậm
                .strokeWidth(4)                    // Đường viền dày hơn
                .fillColor(0x3010B981);            // Màu xanh lá trong suốt đậm hơn
        
        mMap.addPolygon(vietnamBoundary);
        
        // Thêm text overlay để làm rõ đây là Việt Nam
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(16.0, 106.0))  // Giữa Việt Nam
                .title("🇻🇳 LÃNH THỔ VIỆT NAM")
                .snippet("Chỉ được phép chọn vị trí trong vùng này"));
    }
    
    private void updateLocationFromLatLng(LatLng latLng) {
        selectedLocation = latLng;
        
        // Clear existing markers but preserve Vietnam boundary
        mMap.clear();
        
        // Vẽ lại khung viền Việt Nam
        drawVietnamBoundary();
        
        // Kiểm tra xem vị trí có nằm trong phạm vi Việt Nam không
        boolean isInVietnam = isLocationInVietnam(latLng.latitude, latLng.longitude);
        
        // Add new marker với màu khác nhau tùy theo vị trí
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(isInVietnam ? "Vị trí được chọn" : "Vị trí ngoài Việt Nam");
        
        if (!isInVietnam) {
            markerOptions.snippet("Chỉ được chọn địa chỉ trong Việt Nam");
        }
        
        mMap.addMarker(markerOptions);
        
        // Get address from coordinates
        getAddressFromLatLng(latLng);
        
        // Update display
        updateLocationDisplay();
        
        // Cập nhật trạng thái nút xác nhận
        updateConfirmButton(isInVietnam);
        
        // Hiển thị thông báo nếu chọn ngoài Việt Nam
        if (!isInVietnam) {
            Toast.makeText(this, "⚠️ Vị trí này nằm ngoài lãnh thổ Việt Nam!", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Kiểm tra xem tọa độ có nằm trong phạm vi Việt Nam không
     */
    private boolean isLocationInVietnam(double latitude, double longitude) {
        return LocationUtils.isLocationInVietnam(this, latitude, longitude);
    }
    
    /**
     * Cập nhật trạng thái nút xác nhận
     */
    private void updateConfirmButton(boolean isInVietnam) {
        if (isInVietnam) {
            btnConfirm.setEnabled(true);
            btnConfirm.setText("Xác nhận vị trí");
            btnConfirm.setBackgroundTintList(getColorStateList(android.R.color.holo_green_dark));
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Chỉ cho phép chọn địa chỉ ở Việt Nam");
            btnConfirm.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
        }
    }

    private void getAddressFromLatLng(LatLng latLng) {
        try {
            // Sử dụng locale Việt Nam để có định dạng địa chỉ phù hợp
            Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                // Kiểm tra xem có phải địa chỉ ở Việt Nam không
                boolean isVietnamAddress = address.getCountryName() != null && 
                    (address.getCountryName().contains("Vietnam") || 
                     address.getCountryName().contains("Việt Nam") ||
                     address.getCountryCode() != null && address.getCountryCode().equals("VN"));
                
                if (isVietnamAddress || isLocationInVietnam(latLng.latitude, latLng.longitude)) {
                    // Xây dựng địa chỉ theo định dạng Việt Nam
                    StringBuilder addressBuilder = new StringBuilder();
                    
                    // Thêm xã/phường
                    if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
                        addressBuilder.append(address.getSubLocality());
                    } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                        addressBuilder.append(address.getLocality());
                    }
                    
                    // Thêm huyện/quận
                    if (address.getLocality() != null && !address.getLocality().isEmpty() && 
                        !address.getLocality().equals(address.getSubLocality())) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getLocality());
                    }
                    
                    // Thêm tỉnh/thành phố
                    if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(address.getAdminArea());
                    }
                    
                    // Thêm Việt Nam
                    if (addressBuilder.length() > 0) addressBuilder.append(", ");
                    addressBuilder.append("Việt Nam");
                    
                    selectedAddress = addressBuilder.toString();
                    
                    // Nếu không xây dựng được địa chỉ, sử dụng address line
                    if (selectedAddress.equals("Việt Nam") && address.getMaxAddressLineIndex() >= 0) {
                        selectedAddress = address.getAddressLine(0);
                    }
                } else {
                    // Địa chỉ ngoài Việt Nam
                    selectedAddress = "❌ Vị trí ngoài Việt Nam - Không được phép chọn";
                }
                
            } else {
                // Không tìm thấy địa chỉ
                if (isLocationInVietnam(latLng.latitude, latLng.longitude)) {
                    selectedAddress = String.format(Locale.getDefault(), 
                            "%.6f, %.6f, Việt Nam", latLng.latitude, latLng.longitude);
                } else {
                    selectedAddress = "❌ Vị trí ngoài Việt Nam - Không được phép chọn";
                }
            }
            
            Log.d(TAG, "Address: " + selectedAddress);
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            if (isLocationInVietnam(latLng.latitude, latLng.longitude)) {
                selectedAddress = String.format(Locale.getDefault(), 
                        "%.6f, %.6f, Việt Nam", latLng.latitude, latLng.longitude);
            } else {
                selectedAddress = "❌ Vị trí ngoài Việt Nam - Không được phép chọn";
            }
        }
        
        updateLocationDisplay();
    }

    private void updateLocationDisplay() {
        if (selectedAddress != null && !selectedAddress.trim().isEmpty()) {
            tvSelectedLocation.setText(selectedAddress);
        } else {
            tvSelectedLocation.setText("Vị trí được chọn");
        }
    }

    private void confirmLocation() {
        if (selectedLocation != null) {
            // Kiểm tra lại xem vị trí có ở Việt Nam không
            if (isLocationInVietnam(selectedLocation.latitude, selectedLocation.longitude)) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_LATITUDE, selectedLocation.latitude);
                resultIntent.putExtra(EXTRA_LONGITUDE, selectedLocation.longitude);
                resultIntent.putExtra(EXTRA_ADDRESS, selectedAddress);
                
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "❌ Chỉ được phép chọn địa chỉ trong Việt Nam!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Vui lòng chọn một vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Thực hiện tìm kiếm vị trí theo địa chỉ nhập vào
     */
    private void performSearch() {
        String searchQuery = etSearch.getText().toString().trim();
        
        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ cần tìm kiếm", Toast.LENGTH_SHORT).show();
            etSearch.requestFocus();
            return;
        }
        
        // Validate search query
        if (!isValidSearchQuery(searchQuery)) {
            return;
        }
        
        // Ẩn keyboard
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        
        // Disable search button temporarily
        btnSearch.setEnabled(false);
        btnSearch.setText("Đang tìm...");
        
        // Thêm "Việt Nam" vào cuối query nếu chưa có
        if (!searchQuery.toLowerCase().contains("việt nam") && 
            !searchQuery.toLowerCase().contains("vietnam")) {
            searchQuery += ", Việt Nam";
        }
        
        // Thực hiện tìm kiếm bằng Geocoder
        searchLocation(searchQuery);
    }
    
    /**
     * Tìm kiếm địa điểm bằng Geocoder
     */
    private void searchLocation(String query) {
        try {
            // Sử dụng locale Việt Nam để tìm kiếm chính xác hơn
            Geocoder geocoder = new Geocoder(this, new Locale("vi", "VN"));
            List<Address> addresses = geocoder.getFromLocationName(query, 5); // Lấy tối đa 5 kết quả
            
            if (addresses != null && !addresses.isEmpty()) {
                // Tìm địa chỉ đầu tiên nằm trong Việt Nam
                Address selectedAddress = null;
                
                for (Address address : addresses) {
                    double lat = address.getLatitude();
                    double lng = address.getLongitude();
                    
                    // Kiểm tra xem có nằm trong Việt Nam không
                    if (isLocationInVietnam(lat, lng)) {
                        selectedAddress = address;
                        break;
                    }
                }
                
                if (selectedAddress != null) {
                    LatLng location = new LatLng(selectedAddress.getLatitude(), selectedAddress.getLongitude());
                    
                    // Di chuyển camera đến vị trí tìm được với animation mượt
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f), 1500, null);
                    
                    // Cập nhật vị trí được chọn
                    updateLocationFromLatLng(location);
                    
                    // Xóa nội dung search sau khi tìm thành công
                    etSearch.setText("");
                    
                    String locationName = selectedAddress.getFeatureName() != null ? 
                        selectedAddress.getFeatureName() : selectedAddress.getLocality();
                    
                    Toast.makeText(this, "✅ Đã tìm thấy: " + locationName, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "❌ Không tìm thấy địa điểm nào trong Việt Nam với từ khóa này", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "❌ Không tìm thấy địa điểm nào với từ khóa: " + query.replace(", Việt Nam", ""), Toast.LENGTH_LONG).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching location: " + e.getMessage());
            Toast.makeText(this, "❌ Lỗi khi tìm kiếm địa điểm. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
        } finally {
            // Re-enable search button
            btnSearch.setEnabled(true);
            btnSearch.setText("Tìm");
        }
    }
    
    /**
     * Setup các chip suggestions cho tìm kiếm nhanh
     */
    private void setupChipSuggestions() {
        // Lấy data các thành phố lớn từ LocationUtils
        LocationUtils.LocationData[] cities = LocationUtils.getVietnamMajorCities();
        
        // Setup click listeners cho từng chip
        findViewById(R.id.chipHanoi).setOnClickListener(v -> {
            goToCity(cities[0]); // Hà Nội
        });
        
        findViewById(R.id.chipHCM).setOnClickListener(v -> {
            goToCity(cities[1]); // Hồ Chí Minh
        });
        
        findViewById(R.id.chipDanang).setOnClickListener(v -> {
            goToCity(cities[2]); // Đà Nẵng
        });
        
        findViewById(R.id.chipHaiphong).setOnClickListener(v -> {
            goToCity(cities[3]); // Hải Phòng
        });
        
        findViewById(R.id.chipCantho).setOnClickListener(v -> {
            goToCity(cities[4]); // Cần Thơ
        });
    }
    
    /**
     * Di chuyển đến thành phố được chọn
     */
    private void goToCity(LocationUtils.LocationData city) {
        LatLng location = new LatLng(city.latitude, city.longitude);
        
        // Di chuyển camera với animation
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
        
        // Cập nhật vị trí được chọn
        updateLocationFromLatLng(location);
        
        // Hiển thị thông báo
        Toast.makeText(this, "📍 Đã chọn: " + city.address, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Các từ khóa gợi ý tìm kiếm phổ biến
     */
    private void setupSearchSuggestions() {
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etSearch.getText().toString().isEmpty()) {
                // Có thể thêm dropdown suggestions ở đây nếu cần
                etSearch.setHint("VD: Bưu điện trung tâm Hà Nội");
            } else {
                etSearch.setHint("Tìm kiếm địa điểm ở Việt Nam...");
            }
        });
    }
    
    /**
     * Validate input trước khi search
     */
    private boolean isValidSearchQuery(String query) {
        // Kiểm tra độ dài tối thiểu
        if (query.length() < 2) {
            Toast.makeText(this, "Vui lòng nhập ít nhất 2 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Kiểm tra ký tự đặc biệt không hợp lệ
        String invalidChars = "!@#$%^&*()+={}[]|\\:;\"'<>?";
        for (char c : invalidChars.toCharArray()) {
            if (query.contains(String.valueOf(c))) {
                Toast.makeText(this, "Không được sử dụng ký tự đặc biệt trong tìm kiếm", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        
        return true;
    }
}
