# PRM392 Job Finder App

## 📱 Mô tả Project
Ứng dụng Android Job Finder với chức năng đăng ký/đăng nhập, email verification và quản lý công việc. Sử dụng SQLite database và Gmail SMTP cho email service.

## 🚀 Chức năng chính
- ✅ **Đăng ký**: Email + Password + Email Verification
- ✅ **Đăng nhập**: Email + Password với session management
- ✅ **Email Verification**: Gmail SMTP với verification code
- ✅ **Job Management**: Tìm kiếm và ứng tuyển công việc
- ✅ **SQLite Database**: Room database cho data persistence
- ✅ **Location Services**: Google Maps integration

## 🛠️ Tech Stack
- **Android Studio** - IDE
- **Java** - Programming language
- **SQLite + Room** - Local database
- **Gmail SMTP + JavaMail** - Email service
- **Google Maps** - Location services
- **Material Design** - UI components
- **Retrofit** - API client

## 📧 Email Service Setup

### Gmail SMTP Configuration:
1. Tạo Gmail account cho app
2. Bật 2-Factor Authentication
3. Tạo App Password: https://myaccount.google.com/apppasswords
4. Cập nhật credentials trong `GmailEmailService.java`:

```java
private static final String SENDER_EMAIL = "your-app-email@gmail.com";
private static final String SENDER_PASSWORD = "your-app-password";
```

## 📁 Cấu trúc Project (MVC)
```
app/src/main/java/com/example/projectprm392/
├── models/          # User, Job entities
├── views/           # Activities và UI components
├── controllers/     # Business logic controllers
├── database/        # Room database components
├── utils/           # EmailService, GmailEmailService, utilities
└── activities/      # Main activities
```

## 🎮 Cách chạy
1. **Clone project**: `git clone [repo-url]`
2. **Tạo branch**: `develop_android_[tên_bạn]`
3. **Build**: `.\gradlew.bat assembleDebug`
4. **Install**: `.\gradlew.bat installDebug`
5. **Test với tài khoản**: `test@gmail.com` / `123456`

## 📚 Documentation
- **[SQLITE_DATABASE_SETUP.md](SQLITE_DATABASE_SETUP.md)** - Chi tiết về database
- **[LOGIN_SETUP.md](LOGIN_SETUP.md)** - Hướng dẫn login/register

## 🔄 Git Workflow
- **[IMPORTANT]** Tuyệt đối không push thẳng vào `main`
- Tạo branch: `develop_android_[account_name]`
- Commit với message rõ ràng có nghĩa
- Sample: `develop_android_thanhndhe176326`

## 👥 Contributors
- Nhóm PRM392 - FPT University
