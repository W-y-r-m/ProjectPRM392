package com.example.projectprm392.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class ApplicationFormActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_REQUEST = 1001;

    private Toolbar toolbar;
    private TextView tvJobTitle, tvJobCompany;
    private EditText edtMessage, edtOtherFileUrl;
    private CardView cardCvUpload;
    private TextView tvCvStatus, tvCvFileName;
    private MaterialButton btnSubmit;
    private LinearProgressIndicator progressIndicator;

    private JobEntity currentJob;
    private UserEntity currentUser;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    private Uri selectedCvUri;
    private String selectedCvFileName = "";
    
    // File picker launcher
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_form);

        initViews();
        setupToolbar();
        initData();
        loadJobInfo();
        setupListeners();
        setupFilePickerLauncher();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvJobCompany = findViewById(R.id.tvJobCompany);
        edtMessage = findViewById(R.id.edtMessage);
        edtOtherFileUrl = findViewById(R.id.edtOtherFileUrl);
        cardCvUpload = findViewById(R.id.cardCvUpload);
        tvCvStatus = findViewById(R.id.tvCvStatus);
        tvCvFileName = findViewById(R.id.tvCvFileName);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressIndicator = findViewById(R.id.progressIndicator);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ứng tuyển công việc");
        }
    }

    private void initData() {
        // Get job ID from intent
        int jobId = getIntent().getIntExtra("JOB_ID", -1);
        if (jobId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin công việc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load job from database
        currentJob = databaseHelper.getJobById(jobId);
        if (currentJob == null) {
            Toast.makeText(this, "Công việc không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load current user
        String userEmail = sessionManager.getEmail();
        currentUser = databaseHelper.getUserByEmail(userEmail);
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void loadJobInfo() {
        if (currentJob == null) return;

        tvJobTitle.setText(currentJob.getTitle());

        // Load company info
        UserEntity employer = databaseHelper.getUserById(currentJob.getUserId());
        if (employer != null) {
            tvJobCompany.setText(employer.getFullName());
        } else {
            tvJobCompany.setText("Công ty không xác định");
        }
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        cardCvUpload.setOnClickListener(v -> checkPermissionAndOpenFilePicker());
    }

    private void setupFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleSelectedFile(uri);
                    }
                }
            }
        );
    }

    private void checkPermissionAndOpenFilePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    STORAGE_PERMISSION_REQUEST);
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword", 
                             "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Chọn file CV"));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở trình chọn file", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSelectedFile(Uri uri) {
        try {
            // Get file name
            String fileName = getFileName(uri);
            if (fileName == null) {
                fileName = "File đã chọn";
            }
            
            // Check file size (max 5MB)
            long fileSize = getFileSize(uri);
            if (fileSize > 5 * 1024 * 1024) { // 5MB
                Toast.makeText(this, "File quá lớn. Vui lòng chọn file dưới 5MB", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Update UI
            selectedCvUri = uri;
            selectedCvFileName = fileName;
            tvCvStatus.setText("✓ Đã chọn CV");
            tvCvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            tvCvFileName.setText(fileName);
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xử lý file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            // Fallback to last path segment
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

    private long getFileSize(Uri uri) {
        long fileSize = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    fileSize = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return fileSize;
    }

    private void validateAndSubmit() {
        String message = edtMessage.getText().toString().trim();
        String otherFileUrl = edtOtherFileUrl.getText().toString().trim();

        // Validate required fields
        if (TextUtils.isEmpty(message)) {
            edtMessage.setError("Vui lòng nhập thông điệp ứng tuyển");
            edtMessage.requestFocus();
            return;
        }

        if (message.length() < 20) {
            edtMessage.setError("Thông điệp phải có ít nhất 20 ký tự");
            edtMessage.requestFocus();
            return;
        }

        // Show loading
        showLoading(true);

        // Check if user already applied
        if (hasUserAppliedToJob()) {
            showLoading(false);
            Toast.makeText(this, "Bạn đã ứng tuyển vào công việc này rồi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Submit application
        submitApplication(message, otherFileUrl);
    }

    private boolean hasUserAppliedToJob() {
        try {
            return databaseHelper.getApplicationsByUserId(currentUser.getId())
                    .stream()
                    .anyMatch(app -> app.getJobId() == currentJob.getId());
        } catch (Exception e) {
            return false;
        }
    }

    private void submitApplication(String message, String otherFileUrl) {
        try {
            ApplicationEntity application = new ApplicationEntity();
            application.setJobId(currentJob.getId());
            application.setUserId(currentUser.getId());
            application.setMessage(message);
            application.setOtherFileUrl(otherFileUrl.isEmpty() ? null : otherFileUrl);
            
            // Add CV file info if selected
            if (selectedCvUri != null) {
                application.setCvFileName(selectedCvFileName);
                application.setCvFileUri(selectedCvUri.toString());
            }
            
            application.setStatus("PENDING"); // Default status as requested
            application.setAppliedAt(System.currentTimeMillis()); // Use timestamp

            // Insert application
            databaseHelper.insertApplication(application);

            showLoading(false);
            showSuccess();

        } catch (Exception e) {
            showLoading(false);
            Toast.makeText(this, "Lỗi khi gửi đơn ứng tuyển: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }

    private void showSuccess() {
        // Show success dialog with more information
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✅ Thành công!")
            .setMessage("Đã gửi đơn ứng tuyển thành công!\n\n" +
                       "📋 Trạng thái: PENDING\n" +
                       "📞 Nhà tuyển dụng sẽ liên hệ với bạn sớm nhất.")
            .setPositiveButton("Đóng", (dialog, which) -> finish())
            .setCancelable(false)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                Toast.makeText(this, "Cần quyền truy cập file để chọn CV", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
