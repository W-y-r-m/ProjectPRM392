package com.example.projectprm392.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Utility class để xử lý location với Google Maps
 */
public class LocationUtils {
    
    public static class LocationData {
        public double latitude;
        public double longitude;
        public String address;
        
        public LocationData(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }
    }
    
    /**
     * Lấy vị trí hiện tại của người dùng với fallback về Việt Nam
     */
    public static LocationData getCurrentLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            // Nếu không có quyền, trả về vị trí mặc định ở Việt Nam
            return getDefaultVietnamLocation();
        }
        
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        
        if (location != null) {
            // Kiểm tra xem vị trí có ở Việt Nam không (tạm thời theo tọa độ)
            if (isLocationInVietnam(location.getLatitude(), location.getLongitude())) {
                String address = getAddressFromCoordinates(context, location.getLatitude(), location.getLongitude());
                return new LocationData(location.getLatitude(), location.getLongitude(), address);
            }
        }
        
        // Fallback về vị trí mặc định ở Việt Nam
        return getDefaultVietnamLocation();
    }
    
    /**
     * Kiểm tra xem tọa độ có nằm trong phạm vi Việt Nam không (private)
     */
    private static boolean isLocationInVietnam(double latitude, double longitude) {
        // Phạm vi tọa độ gần đúng của Việt Nam
        // Vĩ độ: 8.0 - 23.5
        // Kinh độ: 102.0 - 109.5
        return latitude >= 8.0 && latitude <= 23.5 && 
               longitude >= 102.0 && longitude <= 109.5;
    }
    
    /**
     * Kiểm tra vị trí có nằm trong lãnh thổ Việt Nam không (public version)
     * Sử dụng polygon checking chính xác hơn
     */
    public static boolean isLocationInVietnam(Context context, double latitude, double longitude) {
        // Kiểm tra khung cơ bản trước
        if (latitude < 8.0 || latitude > 23.5 || longitude < 102.0 || longitude > 109.5) {
            return false;
        }
        
        // Kiểm tra chi tiết hơn dựa trên các vùng đặc biệt
        
        // Vùng phía Bắc (từ 20°N trở lên)
        if (latitude >= 20.0) {
            return longitude >= 103.0 && longitude <= 108.5;
        }
        
        // Vùng miền Trung (từ 12°N đến 20°N)
        if (latitude >= 12.0) {
            return longitude >= 102.5 && longitude <= 109.5;
        }
        
        // Vùng miền Nam (dưới 12°N)
        if (latitude >= 8.0) {
            return longitude >= 103.0 && longitude <= 109.2;
        }
        
        return false;
    }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ (hỗ trợ toàn cầu)
     */
    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        try {
            // Sử dụng locale mặc định để hỗ trợ địa chỉ toàn cầu
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                
                // Xây dựng địa chỉ cho bất kỳ quốc gia nào
                StringBuilder addressText = new StringBuilder();
                
                // Thêm đường phố
                if (address.getThoroughfare() != null && !address.getThoroughfare().isEmpty()) {
                    addressText.append(address.getThoroughfare());
                }
                
                // Thêm xã/phường
                if (address.getSubLocality() != null && !address.getSubLocality().isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(address.getSubLocality());
                } else if (address.getLocality() != null && !address.getLocality().isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(address.getLocality());
                }
                
                // Thêm thành phố (nếu khác với sub locality)
                if (address.getLocality() != null && !address.getLocality().isEmpty() && 
                    !address.getLocality().equals(address.getSubLocality())) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(address.getLocality());
                }
                
                // Thêm tỉnh/bang
                if (address.getAdminArea() != null && !address.getAdminArea().isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(address.getAdminArea());
                }
                
                // Thêm quốc gia
                if (address.getCountryName() != null && !address.getCountryName().isEmpty()) {
                    if (addressText.length() > 0) addressText.append(", ");
                    addressText.append(address.getCountryName());
                }
                
                // Nếu không xây dựng được địa chỉ, sử dụng address line đầy đủ
                if (addressText.toString().trim().isEmpty() && address.getMaxAddressLineIndex() >= 0) {
                    addressText.append(address.getAddressLine(0));
                }
                
                return addressText.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
    }
    
    /**
     * Chuyển đổi địa chỉ thành tọa độ (sử dụng locale Việt Nam)
     */
    public static LocationData getCoordinatesFromAddress(Context context, String address) {
        try {
            // Sử dụng locale Việt Nam
            Geocoder geocoder = new Geocoder(context, new Locale("vi", "VN"));
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                return new LocationData(location.getLatitude(), location.getLongitude(), address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Lấy vị trí mặc định ở Việt Nam (Hà Nội)
     */
    public static LocationData getDefaultVietnamLocation() {
        // Tọa độ của Hà Nội, Việt Nam
        double latitude = 21.0285;
        double longitude = 105.8542;
        String address = "Hà Nội, Việt Nam";
        return new LocationData(latitude, longitude, address);
    }
    
    /**
     * Lấy vị trí mặc định cho các thành phố lớn ở Việt Nam
     */
    public static LocationData[] getVietnamMajorCities() {
        return new LocationData[]{
            new LocationData(21.0285, 105.8542, "Hà Nội, Việt Nam"),
            new LocationData(10.8231, 106.6297, "Hồ Chí Minh, Việt Nam"),
            new LocationData(16.0544, 108.2022, "Đà Nẵng, Việt Nam"),
            new LocationData(20.8449, 106.6881, "Hải Phòng, Việt Nam"),
            new LocationData(10.0452, 105.7469, "Cần Thơ, Việt Nam")
        };
    }
    
    /**
     * Tính khoảng cách giữa hai điểm (km)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        
        return location1.distanceTo(location2) / 1000.0; // Convert to km
    }
    
    /**
     * Kiểm tra xem có quyền truy cập location không
     */
    public static boolean hasLocationPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
               == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Định dạng tọa độ để hiển thị
     */
    public static String formatCoordinates(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "%.6f, %.6f", latitude, longitude);
    }
}
