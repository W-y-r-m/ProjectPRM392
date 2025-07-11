package com.example.projectprm392.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_POST_QUOTA = "post_quota";
    private static final String KEY_CURRENT_LATITUDE = "current_latitude";
    private static final String KEY_CURRENT_LONGITUDE = "current_longitude";
    private static final String KEY_ROLE = "role";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void saveUserSession(String userId, String email, String fullName, 
                               String role, String token) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    public void saveUserSession(String userId, String email, String fullName, 
                               String phoneNumber, Boolean gender, String description,
                               Integer postQuota, Double currentLatitude, Double currentLongitude,
                               String role, String token) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.putBoolean(KEY_GENDER, gender != null ? gender : false);
        editor.putString(KEY_DESCRIPTION, description);
        editor.putInt(KEY_POST_QUOTA, postQuota != null ? postQuota : 0);
        editor.putString(KEY_CURRENT_LATITUDE, currentLatitude != null ? currentLatitude.toString() : "");
        editor.putString(KEY_CURRENT_LONGITUDE, currentLongitude != null ? currentLongitude.toString() : "");
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return preferences.getString(KEY_USER_ID, null);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return preferences.getString(KEY_FULL_NAME, null);
    }

    public String getPhoneNumber() {
        return preferences.getString(KEY_PHONE_NUMBER, null);
    }

    public Boolean getGender() {
        return preferences.getBoolean(KEY_GENDER, false);
    }

    public String getDescription() {
        return preferences.getString(KEY_DESCRIPTION, null);
    }

    public Integer getPostQuota() {
        return preferences.getInt(KEY_POST_QUOTA, 0);
    }

    public Double getCurrentLatitude() {
        String lat = preferences.getString(KEY_CURRENT_LATITUDE, "");
        return lat.isEmpty() ? null : Double.parseDouble(lat);
    }

    public Double getCurrentLongitude() {
        String lng = preferences.getString(KEY_CURRENT_LONGITUDE, "");
        return lng.isEmpty() ? null : Double.parseDouble(lng);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
