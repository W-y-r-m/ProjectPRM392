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
     * Lấy vị trí hiện tại của người dùng
     */
    public static LocationData getCurrentLocation(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        
        if (location != null) {
            String address = getAddressFromCoordinates(context, location.getLatitude(), location.getLongitude());
            return new LocationData(location.getLatitude(), location.getLongitude(), address);
        }
        
        return null;
    }
    
    /**
     * Chuyển đổi tọa độ thành địa chỉ
     */
    public static String getAddressFromCoordinates(Context context, double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressText = new StringBuilder();
                
                // Xây dựng địa chỉ từ các thành phần
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    if (i > 0) addressText.append(", ");
                    addressText.append(address.getAddressLine(i));
                }
                
                return addressText.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return "Vị trí: " + latitude + ", " + longitude;
    }
    
    /**
     * Chuyển đổi địa chỉ thành tọa độ
     */
    public static LocationData getCoordinatesFromAddress(Context context, String address) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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
