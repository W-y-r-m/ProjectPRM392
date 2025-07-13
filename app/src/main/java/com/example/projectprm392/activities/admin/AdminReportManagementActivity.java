package com.example.projectprm392.activities.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.admin.ReportManagementAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.ReportEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.JobEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminReportManagementActivity extends AppCompatActivity implements ReportManagementAdapter.OnReportActionListener {

    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private TextView tvReportCount, tvFilteredCount;
    private RecyclerView rvReports;
    private View layoutEmptyState;
    private ProgressBar progressBar;

    private ReportManagementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Toast.makeText(this, "Đang khởi tạo Quản lý báo cáo...", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_admin_report_management);
            
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupListeners();
            loadReports();
            
            Toast.makeText(this, "Khởi tạo thành công!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("AdminReportManagement", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            etSearch = findViewById(R.id.etSearch);
            btnFilter = findViewById(R.id.btnFilter);
            tvReportCount = findViewById(R.id.tvReportCount);
            tvFilteredCount = findViewById(R.id.tvFilteredCount);
            rvReports = findViewById(R.id.rvReports);
            layoutEmptyState = findViewById(R.id.layoutEmptyState);
            progressBar = findViewById(R.id.progressBar);
            
            databaseHelper = new DatabaseHelper(this);
            executorService = Executors.newCachedThreadPool();
            
        } catch (Exception e) {
            android.util.Log.e("AdminReportManagement", "Error in initViews: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý báo cáo");
        }
    }

    private void setupRecyclerView() {
        adapter = new ReportManagementAdapter(this);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(adapter);
    }

    private void setupListeners() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
                updateCounts();
                updateEmptyState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        String[] filterOptions = {"Tất cả", "Chờ xử lý", "Đã giải quyết", "Đã từ chối"};
        String[] filterValues = {"ALL", "PENDING", "RESOLVED", "REJECTED"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lọc báo cáo");
        builder.setSingleChoiceItems(filterOptions, getFilterIndex(), (dialog, which) -> {
            currentFilter = filterValues[which];
            applyFilter();
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private int getFilterIndex() {
        switch (currentFilter) {
            case "PENDING": return 1;
            case "RESOLVED": return 2;
            case "REJECTED": return 3;
            default: return 0;
        }
    }

    private void applyFilter() {
        adapter.applyFilter(currentFilter);
        updateCounts();
        updateEmptyState();
        btnFilter.setText(getFilterDisplayName(currentFilter));
    }

    private String getFilterDisplayName(String filter) {
        switch (filter) {
            case "PENDING": return "Chờ xử lý";
            case "RESOLVED": return "Đã giải quyết";
            case "REJECTED": return "Đã từ chối";
            default: return "Tất cả";
        }
    }

    private void loadReports() {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                List<ReportEntity> reports = databaseHelper.getAllReports();
                
                runOnUiThread(() -> {
                    adapter.updateReports(reports);
                    // Apply current filter after loading reports
                    adapter.applyFilter(currentFilter);
                    updateCounts();
                    updateEmptyState();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã tải " + reports.size() + " báo cáo", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                android.util.Log.e("AdminReportManagement", "Error loading reports", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateCounts() {
        List<ReportEntity> filteredReports = adapter.getFilteredReports();
        tvFilteredCount.setText(String.valueOf(filteredReports.size()));
        
        // Always show total count (all reports regardless of filter)
        List<ReportEntity> allReports = adapter.getAllReports();
        tvReportCount.setText(String.valueOf(allReports.size()));
    }

    private void updateEmptyState() {
        List<ReportEntity> filteredReports = adapter.getFilteredReports();
        if (filteredReports.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
        }
    }

    // ReportManagementAdapter.OnReportActionListener implementation
    @Override
    public void onReportClick(ReportEntity report) {
        showReportDetailsDialog(report);
    }

    @Override
    public void onResolveReport(ReportEntity report) {
        updateReportStatus(report, "resolved");
    }

    @Override
    public void onRejectReport(ReportEntity report) {
        updateReportStatus(report, "rejected");
    }

    @Override
    public void onDeleteReport(ReportEntity report) {
        showDeleteConfirmDialog(report);
    }

    private void updateReportStatus(ReportEntity report, String newStatus) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String action = newStatus.equals("resolved") ? "giải quyết" : "từ chối";
        
        builder.setTitle("Xác nhận")
               .setMessage("Bạn có chắc muốn " + action + " báo cáo này?")
               .setPositiveButton("Đồng ý", (dialog, which) -> {
                   
                   progressBar.setVisibility(View.VISIBLE);
                   
                   executorService.execute(() -> {
                       try {
                           report.status = newStatus;
                           boolean success = databaseHelper.updateReport(report);
                           
                           runOnUiThread(() -> {
                               progressBar.setVisibility(View.GONE);
                               if (success) {
                                   Toast.makeText(this, "Đã " + action + " báo cáo thành công", Toast.LENGTH_SHORT).show();
                                   // Smart filter: switch to appropriate filter based on new status
                                   if (newStatus.equals("resolved")) {
                                       currentFilter = "RESOLVED";
                                       btnFilter.setText("Đã giải quyết");
                                   } else {
                                       currentFilter = "REJECTED";
                                       btnFilter.setText("Đã từ chối");
                                   }
                                   // Just reapply filter without reloading from database
                                   adapter.applyFilter(currentFilter);
                                   updateCounts();
                                   updateEmptyState();
                               } else {
                                   Toast.makeText(this, "Lỗi " + action + " báo cáo", Toast.LENGTH_SHORT).show();
                               }
                           });
                           
                       } catch (Exception e) {
                           android.util.Log.e("AdminReportManagement", "Error updating report status", e);
                           runOnUiThread(() -> {
                               progressBar.setVisibility(View.GONE);
                               Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                           });
                       }
                   });
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void showDeleteConfirmDialog(ReportEntity report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
               .setMessage("Bạn có chắc muốn xóa báo cáo này?\n\nHành động này không thể hoàn tác!")
               .setIcon(R.drawable.ic_delete)
               .setPositiveButton("Xóa", (dialog, which) -> {
                   deleteReport(report);
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void deleteReport(ReportEntity report) {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                boolean success = databaseHelper.deleteReport(report.reportId);
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(this, "Đã xóa báo cáo thành công", Toast.LENGTH_SHORT).show();
                        // Reload reports from database to update total count
                        loadReports(); // This will refresh both total and filtered counts
                    } else {
                        Toast.makeText(this, "Lỗi xóa báo cáo", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e("AdminReportManagement", "Error deleting report", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi xóa báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showReportDetailsDialog(ReportEntity report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Create a detailed view for report info
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Get reporter info
        UserEntity reporter = databaseHelper.getUserById(report.reporterId);
        String reporterName = reporter != null ? reporter.getFullName() : "Không xác định";
        String reporterEmail = reporter != null ? reporter.getEmail() : "Không có";
        
        // Get target user info
        UserEntity targetUser = databaseHelper.getUserById(report.targetUserId);
        String targetName = targetUser != null ? targetUser.getFullName() : "Không xác định";
        String targetEmail = targetUser != null ? targetUser.getEmail() : "Không có";
        
        // Get job info if exists
        String jobInfo = "Không có";
        if (report.jobId != null && !report.jobId.isEmpty()) {
            JobEntity job = databaseHelper.getJobByJobId(report.jobId);
            if (job != null) {
                jobInfo = job.getTitle() + " (ID: " + job.getJobId() + ")";
            }
        }
        
        // Create text views for report details
        addDetailRow(layout, "👤 Người báo cáo:", reporterName + " (" + reporterEmail + ")");
        addDetailRow(layout, "🎯 Người bị báo cáo:", targetName + " (" + targetEmail + ")");
        addDetailRow(layout, "💼 Bài đăng liên quan:", jobInfo);
        addDetailRow(layout, "📝 Nội dung báo cáo:", report.content);
        
        // Status
        String status = getStatusDisplay(report.status);
        addDetailRow(layout, "📊 Trạng thái:", status);
        
        // Created date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        addDetailRow(layout, "📅 Ngày tạo:", sdf.format(new java.util.Date(report.createdAt)));
        
        builder.setView(layout);
        builder.setTitle("📄 Chi tiết báo cáo");
        builder.setPositiveButton("Đóng", null);
        
        // Add action buttons based on status
        if ("pending".equals(report.status)) {
            builder.setNeutralButton("✅ Giải quyết", (dialog, which) -> {
                updateReportStatus(report, "resolved");
            });
            builder.setNegativeButton("❌ Từ chối", (dialog, which) -> {
                updateReportStatus(report, "rejected");
            });
        }
        
        builder.show();
    }
    
    private void addDetailRow(android.widget.LinearLayout parent, String label, String value) {
        android.widget.TextView tvLabel = new android.widget.TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(getResources().getColor(R.color.text_secondary));
        tvLabel.setTextSize(12);
        tvLabel.setPadding(0, 16, 0, 4);
        
        android.widget.TextView tvValue = new android.widget.TextView(this);
        tvValue.setText(value != null ? value : "Không có");
        tvValue.setTextColor(getResources().getColor(R.color.text_primary));
        tvValue.setTextSize(14);
        tvValue.setPadding(0, 0, 0, 8);
        
        parent.addView(tvLabel);
        parent.addView(tvValue);
    }
    
    private String getStatusDisplay(String status) {
        switch (status) {
            case "pending": return "🔄 Chờ xử lý";
            case "resolved": return "✅ Đã giải quyết";
            case "rejected": return "❌ Đã từ chối";
            default: return "❓ Không xác định";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_user_management_menu, menu); // Reuse existing menu
        
        // Add a test data menu item
        menu.add(0, 999, 0, "Tạo dữ liệu test")
            .setIcon(R.drawable.ic_add)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            loadReports();
            return true;
        } else if (item.getItemId() == 999) { // Test data menu item
            createTestData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createTestData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tạo dữ liệu test")
               .setMessage("Bạn có muốn tạo một số báo cáo mẫu để test không?\n\nSẽ tạo:\n- 3 user test\n- 2 job test\n- 5 báo cáo với các trạng thái khác nhau")
               .setPositiveButton("Tạo", (dialog, which) -> {
                   generateTestData();
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void generateTestData() {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                // Create test users
                createTestUsers();
                
                // Create test jobs
                createTestJobs();
                
                // Create test reports
                createTestReports();
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã tạo dữ liệu test thành công!", Toast.LENGTH_SHORT).show();
                    loadReports(); // Reload to show new data
                });
                
            } catch (Exception e) {
                android.util.Log.e("AdminReportManagement", "Error creating test data", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tạo dữ liệu test: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void createTestUsers() {
        java.util.List<UserEntity> existingUsers = databaseHelper.getAllUsers();
        
        // Only create if we don't have enough users
        if (existingUsers.size() < 3) {
            // Create test user 1
            com.example.projectprm392.models.User user1 = new com.example.projectprm392.models.User();
            user1.setUserId(java.util.UUID.randomUUID());
            user1.setFullName("Nguyễn Văn A");
            user1.setEmail("nguyenvana@testreport.com");
            user1.setPassword("123456");
            user1.setPhoneNumber("0901234567");
            user1.setRole("user");
            user1.setIsVerified(true);
            databaseHelper.insertUser(user1);
            
            // Create test user 2
            com.example.projectprm392.models.User user2 = new com.example.projectprm392.models.User();
            user2.setUserId(java.util.UUID.randomUUID());
            user2.setFullName("Trần Thị B");
            user2.setEmail("tranthib@testreport.com");
            user2.setPassword("123456");
            user2.setPhoneNumber("0901234568");
            user2.setRole("user");
            user2.setIsVerified(true);
            databaseHelper.insertUser(user2);
            
            // Create test user 3
            com.example.projectprm392.models.User user3 = new com.example.projectprm392.models.User();
            user3.setUserId(java.util.UUID.randomUUID());
            user3.setFullName("Lê Văn C");
            user3.setEmail("levanc@testreport.com");
            user3.setPassword("123456");
            user3.setPhoneNumber("0901234569");
            user3.setRole("user");
            user3.setIsVerified(true);
            databaseHelper.insertUser(user3);
        }
    }

    private void createTestJobs() {
        java.util.List<UserEntity> users = databaseHelper.getAllUsers();
        if (users.size() < 1) return;
        
        UserEntity user = users.get(0);
        
        // Create test job 1
        JobEntity job1 = new JobEntity();
        job1.setJobId("test_job_report_1");
        job1.setUserId(user.getId()); // Use getId() which returns int
        job1.setTitle("Tuyển Developer Java");
        job1.setDescription("Cần tuyển developer Java có kinh nghiệm 2+ năm làm việc với Spring Boot");
        job1.setSalary("15000000 VND");
        job1.setLocation("Hà Nội");
        job1.setPostType("JOB_POSTING");
        job1.setExperienceLevel("2+ năm");
        job1.setNeededAmount(2);
        job1.setWorkingTime("8h/ngày");
        job1.setIsActive(true);
        job1.setCreatedAt(new java.util.Date());
        databaseHelper.insertJob(job1);
        
        // Create test job 2
        if (users.size() > 1) {
            JobEntity job2 = new JobEntity();
            job2.setJobId("test_job_report_2");
            job2.setUserId(users.get(1).getId()); // Use getId() which returns int
            job2.setTitle("Tìm việc Designer UI/UX");
            job2.setDescription("Tôi là designer có 3 năm kinh nghiệm, tìm việc remote hoặc part-time");
            job2.setSalary("12000000 VND");
            job2.setLocation("TP.HCM");
            job2.setPostType("JOB_SEEKING");
            job2.setExperienceLevel("3 năm");
            job2.setNeededAmount(1);
            job2.setWorkingTime("Linh hoạt");
            job2.setIsActive(true);
            job2.setCreatedAt(new java.util.Date());
            databaseHelper.insertJob(job2);
        }
    }

    private void createTestReports() {
        java.util.List<UserEntity> users = databaseHelper.getAllUsers();
        java.util.List<JobEntity> jobs = databaseHelper.getAllJobs();
        
        if (users.size() < 2) return;
        
        // Report 1: Pending - User 1 reports User 2 about inappropriate job posting
        ReportEntity report1 = new ReportEntity(
            users.get(0).getUserId(), // reporter
            users.get(1).getUserId(), // target user
            jobs.size() > 0 ? jobs.get(0).getJobId() : "", // job id
            "Bài đăng này có nội dung không phù hợp và có dấu hiệu lừa đảo. Yêu cầu mức lương quá cao so với công việc mô tả và thông tin liên hệ không rõ ràng."
        );
        report1.status = "pending";
        databaseHelper.insertReport(report1);
        
        // Report 2: Pending - User 2 reports User 1 for harassment
        ReportEntity report2 = new ReportEntity(
            users.get(1).getUserId(), // reporter
            users.get(0).getUserId(), // target user
            "", // no specific job
            "Người dùng này đã gửi tin nhắn quấy rối và có hành vi không phù hợp trong quá trình liên lệ về công việc. Yêu cầu xử lý nghiêm khắc."
        );
        report2.status = "pending";
        databaseHelper.insertReport(report2);
        
        // Report 3: Pending - Fake job posting report
        if (users.size() > 2) {
            ReportEntity report3 = new ReportEntity(
                users.get(2).getUserId(), // reporter
                users.get(1).getUserId(), // target user
                jobs.size() > 1 ? jobs.get(1).getJobId() : "", // job id
                "Bài đăng tuyển dụng này có dấu hiệu giả mạo. Thông tin công ty không chính xác và yêu cầu nộp phí đào tạo ban đầu."
            );
            report3.status = "pending";
            databaseHelper.insertReport(report3);
        }
        
        // Report 4: Resolved - Already handled report
        ReportEntity report4 = new ReportEntity(
            users.get(0).getUserId(), // reporter
            users.get(1).getUserId(), // target user
            "", // no specific job
            "Báo cáo về vi phạm quy định đăng bài. Người dùng đã đăng nhiều bài trùng lặp và spam."
        );
        report4.status = "resolved";
        databaseHelper.insertReport(report4);
        
        // Report 5: Rejected - Invalid report
        ReportEntity report5 = new ReportEntity(
            users.get(1).getUserId(), // reporter
            users.get(0).getUserId(), // target user
            "", // no specific job
            "Báo cáo không có căn cứ rõ ràng. Chỉ là hiểu lầm trong giao tiếp về điều kiện công việc."
        );
        report5.status = "rejected";
        databaseHelper.insertReport(report5);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
