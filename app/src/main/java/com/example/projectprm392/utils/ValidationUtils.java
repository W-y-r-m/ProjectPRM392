package com.example.projectprm392.utils;

import android.text.TextUtils;
import android.util.Patterns;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ValidationUtils {
    
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    
    public static boolean isValidPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        // Password phải có ít nhất 6 ký tự
        return password.length() >= 6;
    }
    
    public static boolean isValidFullName(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return false;
        }
        return fullName.trim().length() >= 2;
    }
    
    public static String getEmailError(String email) {
        if (TextUtils.isEmpty(email)) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không hợp lệ";
        }
        return null;
    }
    
    public static String getPasswordError(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Mật khẩu không được để trống";
        }
        if (!isValidPassword(password)) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        return null;
    }
    
    public static String getFullNameError(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "Họ và tên không được để trống";
        }
        if (!isValidFullName(fullName)) {
            return "Họ và tên phải có ít nhất 2 ký tự";
        }
        return null;
    }
    
    /**
     * Hash password using SHA-256
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // Fallback: return password as is (not recommended for production)
            return password;
        }
    }
}
