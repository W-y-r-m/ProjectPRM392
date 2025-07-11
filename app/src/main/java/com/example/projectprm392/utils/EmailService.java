package com.example.projectprm392.utils;

/**
 * EmailService - Wrapper class for Gmail SMTP email service
 * Chuyển từ EmailJS sang Gmail SMTP để hỗ trợ Android app
 */
public class EmailService {
    
    public interface EmailCallback {
        void onSuccess(String verificationCode);
        void onError(String error);
    }
    
    /**
     * Gửi email xác thực sử dụng Gmail SMTP
     */
    public static void sendVerificationEmail(String toEmail, String userName, EmailCallback callback) {
        sendVerificationEmailWithContext(null, toEmail, userName, callback);
    }
    
    /**
     * Gửi email xác thực với context sử dụng Gmail SMTP
     */
    public static void sendVerificationEmailWithContext(Object context, String toEmail, String userName, EmailCallback callback) {
        // Create adapter to convert callback types
        GmailEmailService.EmailCallback gmailCallback = createCallbackAdapter(callback);
        
        // Delegate to GmailEmailService
        GmailEmailService.sendVerificationEmailWithContext(context, toEmail, userName, gmailCallback);
    }
    
    /**
     * Gửi email password reset sử dụng Gmail SMTP
     */
    public static void sendPasswordResetEmail(String toEmail, String userName, EmailCallback callback) {
        sendPasswordResetEmailWithContext(null, toEmail, userName, callback);
    }
    
    /**
     * Gửi email password reset với context sử dụng Gmail SMTP
     */
    public static void sendPasswordResetEmailWithContext(Object context, String toEmail, String userName, EmailCallback callback) {
        // Create adapter to convert callback types
        GmailEmailService.EmailCallback gmailCallback = createCallbackAdapter(callback);
        
        // Delegate to GmailEmailService
        GmailEmailService.sendPasswordResetEmailWithContext(context, toEmail, userName, gmailCallback);
    }

    /**
     * Helper method để tạo callback adapter
     */
    private static GmailEmailService.EmailCallback createCallbackAdapter(EmailCallback callback) {
        return new GmailEmailService.EmailCallback() {
            @Override
            public void onSuccess(String verificationCode) {
                callback.onSuccess(verificationCode);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        };
    }
}
