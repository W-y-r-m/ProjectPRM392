package com.example.projectprm392.activities.admin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.JobManagementAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminJobManagementActivity extends AppCompatActivity implements JobManagementAdapter.OnJobActionListener {

    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private TextView tvJobCount, tvFilteredCount;
    private RecyclerView rvJobs;
    private View layoutEmptyState;
    private ProgressBar progressBar;

    private JobManagementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;

    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Toast.makeText(this, "Đang khởi tạo Quản lý bài đăng...", Toast.LENGTH_SHORT).show();
            android.util.Log.d("AdminJobManagement", "Starting onCreate");
            setContentView(R.layout.activity_admin_job_management);
            android.util.Log.d("AdminJobManagement", "Content view set");
            
            initViews();
            android.util.Log.d("AdminJobManagement", "Views initialized");
            
            setupToolbar();
            android.util.Log.d("AdminJobManagement", "Toolbar setup");
            
            setupRecyclerView();
            android.util.Log.d("AdminJobManagement", "RecyclerView setup");
            
            setupListeners();
            android.util.Log.d("AdminJobManagement", "Listeners setup");
            
            loadJobs();
            android.util.Log.d("AdminJobManagement", "Jobs loaded");
            
            Toast.makeText(this, "Khởi tạo thành công!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("AdminJobManagement", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            etSearch = findViewById(R.id.etSearch);
            btnFilter = findViewById(R.id.btnFilter);
            tvJobCount = findViewById(R.id.tvJobCount);
            tvFilteredCount = findViewById(R.id.tvFilteredCount);
            rvJobs = findViewById(R.id.rvJobs);
            layoutEmptyState = findViewById(R.id.layoutEmptyState);
            progressBar = findViewById(R.id.progressBar);
            
            databaseHelper = new DatabaseHelper(this);
            executorService = Executors.newCachedThreadPool();
            
        } catch (Exception e) {
            android.util.Log.e("AdminJobManagement", "Error in initViews: " + e.getMessage(), e);
            throw e;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý bài đăng");
        }
    }

    private void setupRecyclerView() {
        adapter = new JobManagementAdapter(this);
        rvJobs.setLayoutManager(new LinearLayoutManager(this));
        rvJobs.setAdapter(adapter);
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
        String[] filterOptions = {"Tất cả", "Đang hoạt động", "Đã khóa", "Tìm việc", "Tuyển dụng"};
        String[] filterValues = {"ALL", "ACTIVE", "INACTIVE", "JOB_SEEKING", "JOB_POSTING"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Lọc bài đăng");
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
            case "ACTIVE": return 1;
            case "INACTIVE": return 2;
            case "JOB_SEEKING": return 3;
            case "JOB_POSTING": return 4;
            default: return 0;
        }
    }

    private void applyFilter() {
        adapter.applyFilter(currentFilter);
        updateCounts();
        updateEmptyState();
    }

    private void loadJobs() {
        android.util.Log.d("AdminJobManagement", "Loading jobs from database...");
        showLoading(true);
        executorService.execute(() -> {
            try {
                List<JobEntity> jobs = databaseHelper.getAllJobs();
                android.util.Log.d("AdminJobManagement", "Loaded " + jobs.size() + " jobs");
                for (JobEntity job : jobs) {
                    android.util.Log.d("AdminJobManagement", "Job: " + job.getTitle() + " - " + job.getPostType() + " - Active: " + job.getIsActive());
                }
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.updateJobs(jobs);
                    updateCounts();
                    updateEmptyState();
                });
            } catch (Exception e) {
                android.util.Log.e("AdminJobManagement", "Error loading jobs: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvJobs.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateCounts() {
        List<JobEntity> allJobs = databaseHelper.getAllJobs();
        List<JobEntity> filteredJobs = adapter.getFilteredJobs();
        
        tvJobCount.setText("Tổng: " + allJobs.size() + " bài đăng");
        tvFilteredCount.setText("Hiển thị: " + filteredJobs.size());
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // JobManagementAdapter.OnJobActionListener implementation
    @Override
    public void onJobClick(JobEntity job) {
        showJobDetailsDialog(job);
    }

    @Override
    public void onEditJob(JobEntity job) {
        showJobFormDialog(job);
    }

    @Override
    public void onDeleteJob(JobEntity job) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa bài đăng \"" + job.getTitle() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    executorService.execute(() -> {
                        try {
                            databaseHelper.deleteJob(job.getId());
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                                loadJobs();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Lỗi xóa bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onToggleJobStatus(JobEntity job) {
        String action = job.getIsActive() ? "khóa" : "mở khóa";
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận " + action)
                .setMessage("Bạn có chắc muốn " + action + " bài đăng \"" + job.getTitle() + "\"?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    android.util.Log.d("AdminJobManagement", "Toggling job status for: " + job.getTitle());
                    android.util.Log.d("AdminJobManagement", "Current status: " + job.getIsActive());
                    
                    // Toggle status
                    boolean newStatus = !job.getIsActive();
                    job.setIsActive(newStatus);
                    
                    android.util.Log.d("AdminJobManagement", "New status: " + newStatus);
                    
                    // Update in database
                    executorService.execute(() -> {
                        try {
                            boolean success = databaseHelper.updateJob(job);
                            
                            runOnUiThread(() -> {
                                if (success) {
                                    android.util.Log.d("AdminJobManagement", "Job status toggle successful");
                                    String statusMessage = newStatus ? "mở khóa" : "khóa";
                                    Toast.makeText(this, "Đã " + statusMessage + " bài đăng thành công", Toast.LENGTH_SHORT).show();
                                    loadJobs();
                                } else {
                                    android.util.Log.e("AdminJobManagement", "Job status toggle failed");
                                    Toast.makeText(this, "Cập nhật trạng thái thất bại", Toast.LENGTH_SHORT).show();
                                    // Revert status if failed
                                    job.setIsActive(!newStatus);
                                }
                            });
                        } catch (Exception e) {
                            android.util.Log.e("AdminJobManagement", "Error toggling job status: " + e.getMessage(), e);
                            runOnUiThread(() -> {
                                Toast.makeText(this, "Lỗi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                // Revert status if failed
                                job.setIsActive(!newStatus);
                            });
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showJobDetailsDialog(JobEntity job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_job_details, null);
        
        // Populate job details
        TextView tvTitle = dialogView.findViewById(R.id.tvJobTitle);
        TextView tvDescription = dialogView.findViewById(R.id.tvJobDescription);
        TextView tvSalary = dialogView.findViewById(R.id.tvJobSalary);
        TextView tvLocation = dialogView.findViewById(R.id.tvJobLocation);
        TextView tvType = dialogView.findViewById(R.id.tvJobType);
        TextView tvStatus = dialogView.findViewById(R.id.tvJobStatus);
        TextView tvCreatedAt = dialogView.findViewById(R.id.tvJobCreatedAt);
        
        tvTitle.setText(job.getTitle());
        tvDescription.setText(job.getDescription());
        tvSalary.setText(job.getSalary());
        tvLocation.setText(job.getLocation());
        tvType.setText(job.getPostType());
        tvStatus.setText(job.getIsActive() ? "Đang hoạt động" : "Đã khóa");
        tvCreatedAt.setText(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "N/A");
        
        builder.setView(dialogView);
        builder.setTitle("Chi tiết bài đăng");
        builder.setPositiveButton("Đóng", null);
        builder.show();
    }

    private void showJobFormDialog(JobEntity jobToEdit) {
        // TODO: Implement edit job dialog if needed
        Toast.makeText(this, "Chức năng chỉnh sửa bài đăng đang phát triển", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_job_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            loadJobs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
