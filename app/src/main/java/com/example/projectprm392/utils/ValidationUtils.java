package com.example.projectprm392.utils;

import android.text.TextUtils;
import android.util.Patterns;

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
}
