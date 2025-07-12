package com.example.projectprm392.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectprm392.R;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class JobDetailActivity extends AppCompatActivity {
    
    private static final int PHONE_PERMISSION_REQUEST = 1001;
    
    private Toolbar toolbar;
    private TextView tvJobTitle, tvJobDescription, tvJobSalary, tvJobLocation;
    private TextView tvJobType, tvExperienceLevel, tvNeededAmount, tvWorkingTime;
    private TextView tvPostTime, tvStatus;
    
    // Employer/Job Seeker Info
    private MaterialCardView cardEmployerInfo;
    private TextView tvEmployerName, tvEmployerPhone, tvEmployerRole;
    
    // Action Buttons
    private MaterialButton btnContactCall, btnApplyJob;
    
    private JobEntity currentJob;
    private UserEntity currentUser;
    private UserEntity jobPoster;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_detail);
        
        initViews();
        setupToolbar();
        initData();
        loadJobDetails();
        setupActionButtons();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        
        // Job details
        tvJobTitle = findViewById(R.id.tvJobTitle);
        tvJobDescription = findViewById(R.id.tvJobDescription);
        tvJobSalary = findViewById(R.id.tvJobSalary);
        tvJobLocation = findViewById(R.id.tvJobLocation);
        tvJobType = findViewById(R.id.tvJobType);
        tvExperienceLevel = findViewById(R.id.tvExperienceLevel);
        tvNeededAmount = findViewById(R.id.tvNeededAmount);
        tvWorkingTime = findViewById(R.id.tvWorkingTime);
        tvPostTime = findViewById(R.id.tvPostTime);
        tvStatus = findViewById(R.id.tvStatus);
        
        // Employer info
        cardEmployerInfo = findViewById(R.id.cardEmployerInfo);
        tvEmployerName = findViewById(R.id.tvEmployerName);
        tvEmployerPhone = findViewById(R.id.tvEmployerPhone);
        tvEmployerRole = findViewById(R.id.tvEmployerRole);
        
        // Action buttons
        btnContactCall = findViewById(R.id.btnContactCall);
        btnApplyJob = findViewById(R.id.btnApplyJob);
        
        // Initialize helpers
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết công việc");
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
        
        // Load job poster
        jobPoster = databaseHelper.getUserById(currentJob.getUserId());
    }
    
    private void loadJobDetails() {
        if (currentJob == null) return;
        
        // Job basic info
        tvJobTitle.setText(currentJob.getTitle());
        tvJobDescription.setText(currentJob.getDescription());
        tvJobSalary.setText(currentJob.getSalary());
        tvJobLocation.setText(currentJob.getLocation());
        tvJobType.setText(currentJob.getJobType());
        tvExperienceLevel.setText(currentJob.getExperienceLevel());
        tvNeededAmount.setText("Cần " + currentJob.getNeededAmount() + " người");
        tvWorkingTime.setText(currentJob.getWorkingTime());
        tvStatus.setText(currentJob.getStatus());
        
        // Format post time
        if (currentJob.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvPostTime.setText("Đăng lúc: " + sdf.format(currentJob.getCreatedAt()));
        }
        
        // Load employer info
        loadEmployerInfo();
    }
      private void loadEmployerInfo() {
        if (jobPoster == null) {
            cardEmployerInfo.setVisibility(View.GONE);
            return;
        }

        tvEmployerName.setText(jobPoster.getFullName());
        
        // Show partially hidden phone number initially
        String phoneNumber = jobPoster.getPhoneNumber();
        if (phoneNumber != null && phoneNumber.length() > 3) {
            String hiddenPhone = "***-***-" + phoneNumber.substring(Math.max(0, phoneNumber.length() - 3));
            tvEmployerPhone.setText(hiddenPhone);
        } else {
            tvEmployerPhone.setText("Không có SĐT");
        }
        
        if ("JOB_SEEKING".equals(currentJob.getPostType())) {
            tvEmployerRole.setText("👤 Người tìm việc");
        } else {
            tvEmployerRole.setText("🏢 Nhà tuyển dụng");
        }
    }
    
    private void setupActionButtons() {
        if (currentJob == null || currentUser == null) {
            btnContactCall.setVisibility(View.GONE);
            btnApplyJob.setVisibility(View.GONE);
            return;
        }
        
        // Check if this is the job poster
        if (currentJob.getUserId() == currentUser.getId()) {
            btnContactCall.setVisibility(View.GONE);
            btnApplyJob.setVisibility(View.GONE);
            return;
        }
        
        if ("JOB_SEEKING".equals(currentJob.getPostType())) {
            // This is a job seeking post - show contact button
            btnContactCall.setVisibility(View.VISIBLE);
            btnContactCall.setText("Liên hệ ngay");
            btnApplyJob.setVisibility(View.GONE);
            
            btnContactCall.setOnClickListener(v -> handleContactAction());
        } else {
            // This is a job posting - show apply button
            btnContactCall.setVisibility(View.GONE);
            btnApplyJob.setVisibility(View.VISIBLE);
            btnApplyJob.setText("Ứng tuyển ngay");
            
            btnApplyJob.setOnClickListener(v -> handleApplyAction());
        }
    }
    
    private void handleContactAction() {
        if (jobPoster == null || jobPoster.getPhoneNumber() == null) {
            Toast.makeText(this, "Không có thông tin liên hệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show dialog to request phone access permission
        new AlertDialog.Builder(this)
            .setTitle("Xin quyền liên hệ")
            .setMessage("Bạn có muốn được cấp quyền liên hệ số điện thoại của " + jobPoster.getFullName() + " không?")
            .setPositiveButton("Đồng ý", (dialog, which) -> {
                showPhoneNumber();
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void showPhoneNumber() {
        if (jobPoster == null || jobPoster.getPhoneNumber() == null) return;

        // Update UI to show full phone number with animation
        tvEmployerPhone.setText(jobPoster.getPhoneNumber());
        
        // Show success toast
        Toast.makeText(this, "✅ Đã cấp quyền xem số điện thoại", Toast.LENGTH_SHORT).show();

        // Show dialog with phone actions
        new AlertDialog.Builder(this)
            .setTitle("📞 Thông tin liên hệ")
            .setMessage("Tên: " + jobPoster.getFullName() + "\n" +
                       "Số điện thoại: " + jobPoster.getPhoneNumber() + "\n\n" +
                       "Bạn có muốn gọi điện ngay bây giờ?")
            .setPositiveButton("📞 Gọi ngay", (dialog, which) -> {
                makePhoneCall(jobPoster.getPhoneNumber());
            })
            .setNegativeButton("💬 Nhắn tin", (dialog, which) -> {
                // Option to send SMS
                Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                smsIntent.setData(Uri.parse("sms:" + jobPoster.getPhoneNumber()));
                smsIntent.putExtra("sms_body", "Xin chào, tôi quan tâm đến bài đăng tìm việc của bạn: " + currentJob.getTitle());
                startActivity(smsIntent);
            })
            .setNeutralButton("Đóng", null)
            .show();
    }
    
    private void makePhoneCall(String phoneNumber) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) 
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CALL_PHONE}, 
                PHONE_PERMISSION_REQUEST);
        } else {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        }
    }
    
    private void handleApplyAction() {
        if (currentJob == null) return;
        
        // Navigate to application form
        Intent intent = new Intent(this, ApplicationFormActivity.class);
        intent.putExtra("JOB_ID", currentJob.getId());
        intent.putExtra("JOB_TITLE", currentJob.getTitle());
        startActivity(intent);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PHONE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (jobPoster != null && jobPoster.getPhoneNumber() != null) {
                    makePhoneCall(jobPoster.getPhoneNumber());
                }
            } else {
                Toast.makeText(this, "Cần quyền gọi điện để thực hiện chức năng này", 
                    Toast.LENGTH_SHORT).show();
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
