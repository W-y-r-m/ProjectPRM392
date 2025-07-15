package com.example.projectprm392.config;

public class PayOSConfig {
    // PayOS Configuration
    public static final String CLIENT_ID = "5a6d3cf0-a773-4383-a422-26f20d592306";
    public static final String API_KEY = "a933a3ef-8981-4781-841d-f655b51bf714";
    public static final String CHECKSUM_KEY = "bf3f536d8c7b5c3af5e87953f9ee69e8092e586f5857a0df32e0884081295ae9";
    public static final String WEBHOOK_URL = "https://9dcd-14-177-249-140.ngrok-free.app/api/payment/payos/webhook";
    
    // PayOS API endpoints - CORRECT DOMAIN: api-merchant.payos.vn
    // ❌ WRONG: api.payos.vn (DOES NOT EXIST)
    // ✅ CORRECT: api-merchant.payos.vn (OFFICIAL DOMAIN)
    public static final String PAYOS_BASE_URL = "https://api-merchant.payos.vn";
    public static final String CREATE_PAYMENT_LINK = "/v2/payment-requests";
    public static final String GET_PAYMENT_INFO = "/v2/payment-requests/{orderCode}";
    public static final String CANCEL_PAYMENT = "/v2/payment-requests/{orderCode}/cancel";
    
    // Default payment values
    public static final int DEFAULT_AMOUNT = 100000; // 100k VND
    public static final int DEFAULT_POST_QUOTA = 10; // 10 lượt đăng tin
    public static final String DEFAULT_DESCRIPTION = "Nap tien dang tin"; // Tối đa 25 ký tự
    
    // Return URLs (HTTP URLs thay vì deep links để tránh PayOS Error Code 201)
    public static final String SUCCESS_URL = "https://prm392.example.com/payment/success";
    public static final String CANCEL_URL = "https://prm392.example.com/payment/cancel";
    
    // Payment status
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_EXPIRED = "EXPIRED";
}
