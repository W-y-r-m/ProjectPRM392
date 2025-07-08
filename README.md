# PRM392 - Android Login App với SQLite Database

## 📱 Mô tả Project
App Android với chức năng đăng nhập/đăng ký sử dụng SQLite database, theo mô hình MVC.

## 🚀 Chức năng chính
- ✅ **Đăng ký**: Email + Password + Thông tin cá nhân
- ✅ **Đăng nhập**: Email + Password  
- ✅ **Logout**: Menu hoặc nút đăng xuất
- ✅ **SQLite Database**: Lưu trữ dữ liệu local với Room
- ✅ **Session Management**: Quản lý phiên đăng nhập

## 🛠️ Tech Stack
- **Android Studio** - IDE
- **Java** - Programming language
- **SQLite + Room** - Local database
- **Material Design** - UI components
- **Retrofit** - API client (backup for future use)

## 📁 Cấu trúc Project (MVC)
```
app/src/main/java/com/example/projectprm392/
├── models/          # User, LoginRequest, LoginResponse
├── views/           # LoginActivity, RegisterActivity  
├── controllers/     # LoginController
├── database/        # Room database components
├── api/            # API services
└── utils/          # SessionManager, ValidationUtils
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
