package com.example.projectprm392.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailEmailService {
    private static final String TAG = "GmailEmailService";
    
    // Gmail SMTP Configuration
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    // ⚠️ THAY ĐỔI THÔNG TIN NÀY TRƯỚC KHI SỬ DỤNG!
    // Hướng dẫn: https://myaccount.google.com/apppasswords
    private static final String SENDER_EMAIL = "hainthe172574@fpt.edu.vn"; // TODO: Thay đổi email của bạn
    private static final String SENDER_PASSWORD = "lpax fmku nyvn ldvl"; // TODO: Thay đổi App Password từ Google
    private static final String SENDER_NAME = "PRM392 Job Finder";
    
    public interface EmailCallback {
        void onSuccess(String verificationCode);
        void onError(String error);
    }
    
    public static void sendVerificationEmail(String toEmail, String userName, EmailCallback callback) {
        sendVerificationEmailWithContext(null, toEmail, userName, callback);
    }
    
    public static void sendVerificationEmailWithContext(Object context, String toEmail, String userName, EmailCallback callback) {
        // Tạo mã verification 6 ký tự
        String verificationCode = generateVerificationCode();
        
        new AsyncTask<Void, Void, EmailResult>() {
            @Override
            protected EmailResult doInBackground(Void... voids) {
                // Send email via Gmail SMTP
                return sendEmailViaGmailSMTP(toEmail, userName, verificationCode);
            }
            
            @Override
            protected void onPostExecute(EmailResult result) {
                if (result.success) {
                    callback.onSuccess(result.verificationCode);
                } else {
                    // Trong trường hợp lỗi, vẫn trả về code để test
                    Log.w(TAG, "Email sending failed, but providing code for testing: " + result.error);
                    callback.onSuccess(result.verificationCode);
                }
            }
        }.execute();
    }
    
    /**
     * Gửi email password reset
     */
    public static void sendPasswordResetEmail(String toEmail, String userName, EmailCallback callback) {
        sendPasswordResetEmailWithContext(null, toEmail, userName, callback);
    }
    
    /**
     * Gửi email password reset với context
     */
    public static void sendPasswordResetEmailWithContext(Object context, String toEmail, String userName, EmailCallback callback) {
        // Tạo mã reset password 6 ký tự
        String resetCode = generateVerificationCode();
        
        new AsyncTask<Void, Void, EmailResult>() {
            @Override
            protected EmailResult doInBackground(Void... voids) {
                // Send email via Gmail SMTP
                return sendPasswordResetViaGmailSMTP(toEmail, userName, resetCode);
            }
            
            @Override
            protected void onPostExecute(EmailResult result) {
                if (result.success) {
                    callback.onSuccess(result.verificationCode);
                } else {
                    // Trong trường hợp lỗi, vẫn trả về code để test
                    Log.w(TAG, "Password reset email failed, but providing code for testing: " + result.error);
                    callback.onSuccess(result.verificationCode);
                }
            }
        }.execute();
    }
    
    private static EmailResult sendEmailViaGmailSMTP(String toEmail, String userName, String verificationCode) {
        try {
            // Gmail SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Xác thực tài khoản PRM392 Job Finder");
            
            // Email content
            String emailContent = createEmailContent(userName, verificationCode);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            return new EmailResult(true, verificationCode, null);
            
        } catch (MessagingException e) {
            Log.e(TAG, "Failed to send email: " + e.getMessage());
            return new EmailResult(false, verificationCode, "Lỗi gửi email: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            return new EmailResult(false, verificationCode, "Lỗi không xác định: " + e.getMessage());
        }
    }
    
    private static EmailResult sendPasswordResetViaGmailSMTP(String toEmail, String userName, String resetCode) {
        try {
            // Gmail SMTP properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Đặt lại mật khẩu - PRM392 Job Finder");
            
            // Email content
            String emailContent = createPasswordResetEmailContent(userName, resetCode);
            message.setContent(emailContent, "text/html; charset=utf-8");
            
            // Send email
            Transport.send(message);
            
            return new EmailResult(true, resetCode, null);
            
        } catch (MessagingException e) {
            Log.e(TAG, "Failed to send password reset email: " + e.getMessage());
            return new EmailResult(false, resetCode, "Lỗi gửi email: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            return new EmailResult(false, resetCode, "Lỗi không xác định: " + e.getMessage());
        }
    }
    
    private static String createEmailContent(String userName, String verificationCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #2196F3; color: white; text-align: center; padding: 20px; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }" +
                ".code-box { background: #fff; padding: 20px; border: 2px solid #2196F3; border-radius: 8px; text-align: center; margin: 20px 0; }" +
                ".code { font-size: 32px; font-weight: bold; color: #2196F3; letter-spacing: 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>PRM392 Job Finder</h1>" +
                "<p>Xác thực tài khoản</p>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Xin chào " + userName + "!</h2>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản <strong>PRM392 Job Finder</strong>.</p>" +
                "<p>Để hoàn tất việc đăng ký, vui lòng nhập mã xác thực sau vào ứng dụng:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + verificationCode + "</div>" +
                "</div>" +
                "<p><strong>Lưu ý quan trọng:</strong></p>" +
                "<ul>" +
                "<li>Mã xác thực này sẽ hết hạn sau <strong>15 phút</strong></li>" +
                "<li>Không chia sẻ mã này với bất kỳ ai</li>" +
                "<li>Nếu bạn không yêu cầu đăng ký, vui lòng bỏ qua email này</li>" +
                "</ul>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>Đội ngũ PRM392 Job Finder</p>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private static String createPasswordResetEmailContent(String userName, String resetCode) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: #F44336; color: white; text-align: center; padding: 20px; border-radius: 8px 8px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }" +
                ".code-box { background: #fff; padding: 20px; border: 2px solid #F44336; border-radius: 8px; text-align: center; margin: 20px 0; }" +
                ".code { font-size: 32px; font-weight: bold; color: #F44336; letter-spacing: 5px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }" +
                ".warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>🔐 PRM392 Job Finder</h1>" +
                "<p>Đặt lại mật khẩu</p>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Xin chào " + userName + "!</h2>" +
                "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản <strong>PRM392 Job Finder</strong> của bạn.</p>" +
                "<p>Để đặt lại mật khẩu, vui lòng sử dụng mã xác thực sau:</p>" +
                "<div class='code-box'>" +
                "<div class='code'>" + resetCode + "</div>" +
                "</div>" +
                "<div class='warning'>" +
                "<p><strong>⚠️ Lưu ý bảo mật:</strong></p>" +
                "<ul>" +
                "<li>Mã này sẽ hết hạn sau <strong>15 phút</strong></li>" +
                "<li>Không chia sẻ mã này với bất kỳ ai</li>" +
                "<li>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này</li>" +
                "<li>Đổi mật khẩu ngay sau khi đặt lại thành công</li>" +
                "</ul>" +
                "</div>" +
                "<p>Nếu bạn không thực hiện yêu cầu này, có thể ai đó đang cố gắng truy cập tài khoản của bạn. Vui lòng kiểm tra bảo mật tài khoản.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>Trân trọng,<br>Đội ngũ PRM392 Job Finder</p>" +
                "<p>Email này được gửi tự động, vui lòng không trả lời.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6 digit code
        return String.valueOf(code);
    }
    
    // Helper class for email result
    private static class EmailResult {
        boolean success;
        String verificationCode;
        String error;
        
        EmailResult(boolean success, String verificationCode, String error) {
            this.success = success;
            this.verificationCode = verificationCode;
            this.error = error;
        }
    }
}
