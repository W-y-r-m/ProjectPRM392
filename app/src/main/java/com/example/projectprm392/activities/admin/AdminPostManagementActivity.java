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
import com.example.projectprm392.adapters.admin.PostManagementAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminPostManagementActivity extends AppCompatActivity implements PostManagementAdapter.OnPostActionListener {

    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter;
    private TextView tvPostCount, tvFilteredCount;
    private RecyclerView rvPosts;
    private View layoutEmptyState;
    private ProgressBar progressBar;

    private PostManagementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            Toast.makeText(this, "Đang khởi tạo Quản lý bài đăng...", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_admin_post_management);
            
            initViews();
            setupToolbar();
            setupRecyclerView();
            setupListeners();
            loadPosts();
            
            Toast.makeText(this, "Khởi tạo thành công!", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            android.util.Log.e("AdminPostManagement", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            etSearch = findViewById(R.id.etSearch);
            btnFilter = findViewById(R.id.btnFilter);
            tvPostCount = findViewById(R.id.tvPostCount);
            tvFilteredCount = findViewById(R.id.tvFilteredCount);
            rvPosts = findViewById(R.id.rvPosts);
            layoutEmptyState = findViewById(R.id.layoutEmptyState);
            progressBar = findViewById(R.id.progressBar);
            
            databaseHelper = new DatabaseHelper(this);
            executorService = Executors.newCachedThreadPool();
            
        } catch (Exception e) {
            android.util.Log.e("AdminPostManagement", "Error in initViews: " + e.getMessage(), e);
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
        adapter = new PostManagementAdapter(this);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setAdapter(adapter);
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
        btnFilter.setText(getFilterDisplayName(currentFilter));
    }

    private String getFilterDisplayName(String filter) {
        switch (filter) {
            case "ACTIVE": return "Đang hoạt động";
            case "INACTIVE": return "Đã khóa";
            case "JOB_SEEKING": return "Tìm việc";
            case "JOB_POSTING": return "Tuyển dụng";
            default: return "Tất cả";
        }
    }

    private void loadPosts() {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                List<JobEntity> posts = databaseHelper.getAllJobs();
                
                runOnUiThread(() -> {
                    adapter.updatePosts(posts);
                    // Apply current filter after loading posts
                    adapter.applyFilter(currentFilter);
                    updateCounts();
                    updateEmptyState();
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Đã tải " + posts.size() + " bài đăng", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                android.util.Log.e("AdminPostManagement", "Error loading posts", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateCounts() {
        List<JobEntity> filteredPosts = adapter.getFilteredPosts();
        tvFilteredCount.setText(String.valueOf(filteredPosts.size()));
        
        // Always show total count (all posts regardless of filter)
        List<JobEntity> allPosts = adapter.getAllPosts();
        tvPostCount.setText(String.valueOf(allPosts.size()));
    }

    private void updateEmptyState() {
        List<JobEntity> filteredPosts = adapter.getFilteredPosts();
        if (filteredPosts.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvPosts.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvPosts.setVisibility(View.VISIBLE);
        }
    }

    // PostManagementAdapter.OnPostActionListener implementation
    @Override
    public void onPostClick(JobEntity post) {
        showPostDetailsDialog(post);
    }

    @Override
    public void onEditPost(JobEntity post) {
        showEditPostDialog(post);
    }

    @Override
    public void onDeletePost(JobEntity post) {
        showDeleteConfirmDialog(post);
    }

    @Override
    public void onTogglePostStatus(JobEntity post) {
        togglePostStatus(post);
    }

    @Override
    public void onViewApplicants(JobEntity post) {
        showApplicantsDialog(post);
    }

    private void togglePostStatus(JobEntity post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        boolean isActive = post.getIsActive() != null ? post.getIsActive() : false;
        String action = isActive ? "khóa" : "kích hoạt";
        
        builder.setTitle("Xác nhận")
               .setMessage("Bạn có chắc muốn " + action + " bài đăng \"" + post.getTitle() + "\"?")
               .setPositiveButton("Đồng ý", (dialog, which) -> {
                   
                   progressBar.setVisibility(View.VISIBLE);
                   
                   executorService.execute(() -> {
                       try {
                           post.setIsActive(!isActive);
                           boolean success = databaseHelper.updateJob(post);
                           
                           runOnUiThread(() -> {
                               progressBar.setVisibility(View.GONE);
                               if (success) {
                                   Toast.makeText(this, "Đã " + action + " bài đăng thành công", Toast.LENGTH_SHORT).show();
                                   // Smart filter: switch to appropriate filter based on new status
                                   if (!isActive) {
                                       // Post was activated, show in ACTIVE filter
                                       currentFilter = "ACTIVE";
                                       btnFilter.setText("Đang hoạt động");
                                   } else {
                                       // Post was deactivated, show in INACTIVE filter
                                       currentFilter = "INACTIVE";
                                       btnFilter.setText("Đã khóa");
                                   }
                                   // Just reapply filter without reloading from database
                                   // This keeps total count unchanged for toggle status
                                   adapter.applyFilter(currentFilter);
                                   updateCounts();
                                   updateEmptyState();
                               } else {
                                   Toast.makeText(this, "Lỗi " + action + " bài đăng", Toast.LENGTH_SHORT).show();
                               }
                           });
                           
                       } catch (Exception e) {
                           android.util.Log.e("AdminPostManagement", "Error toggling post status", e);
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

    private void showDeleteConfirmDialog(JobEntity post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
               .setMessage("Bạn có chắc muốn xóa bài đăng \"" + post.getTitle() + "\"?\n\nHành động này không thể hoàn tác!")
               .setIcon(R.drawable.ic_delete)
               .setPositiveButton("Xóa", (dialog, which) -> {
                   deletePost(post);
               })
               .setNegativeButton("Hủy", null)
               .show();
    }

    private void deletePost(JobEntity post) {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                boolean success = databaseHelper.deleteJob(post.getId());
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(this, "Đã xóa bài đăng thành công", Toast.LENGTH_SHORT).show();
                        // Reload posts from database to update total count
                        loadPosts(); // This will refresh both total and filtered counts
                    } else {
                        Toast.makeText(this, "Lỗi xóa bài đăng", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                android.util.Log.e("AdminPostManagement", "Error deleting post", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi xóa bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showEditPostDialog(JobEntity post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_post, null);
        
        // Initialize views
        TextInputEditText etPostTitle = dialogView.findViewById(R.id.etPostTitle);
        TextInputEditText etPostDescription = dialogView.findViewById(R.id.etPostDescription);
        TextInputEditText etPostSalary = dialogView.findViewById(R.id.etPostSalary);
        TextInputEditText etPostLocation = dialogView.findViewById(R.id.etPostLocation);
        AutoCompleteTextView spPostType = dialogView.findViewById(R.id.spPostType);
        TextInputEditText etPostExperience = dialogView.findViewById(R.id.etPostExperience);
        
        // Setup post type dropdown
        String[] postTypeDisplays = {"Tuyển dụng", "Tìm việc"};
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, postTypeDisplays);
        spPostType.setAdapter(adapterType);
        
        // Fill current data
        etPostTitle.setText(post.getTitle());
        etPostDescription.setText(post.getDescription());
        
        // Parse salary to remove currency formatting
        String salaryText = post.getSalary();
        if (salaryText != null && !salaryText.equals("Thỏa thuận")) {
            try {
                String numericSalary = salaryText.replaceAll("[^\\d]", "");
                etPostSalary.setText(numericSalary);
            } catch (Exception e) {
                etPostSalary.setText("");
            }
        }
        
        etPostLocation.setText(post.getLocation());
        
        // Set post type
        if ("JOB_POSTING".equals(post.getPostType())) {
            spPostType.setText("Tuyển dụng", false);
        } else if ("JOB_SEEKING".equals(post.getPostType())) {
            spPostType.setText("Tìm việc", false);
        }
        
        etPostExperience.setText(post.getExperienceLevel() != null ? post.getExperienceLevel() : "");
        
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Cancel button
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        
        // Save button
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            savePostChanges(post, dialogView, dialog);
        });
        
        dialog.show();
    }

    private void savePostChanges(JobEntity post, View dialogView, AlertDialog dialog) {
        // Get views
        TextInputEditText etPostTitle = dialogView.findViewById(R.id.etPostTitle);
        TextInputEditText etPostDescription = dialogView.findViewById(R.id.etPostDescription);
        TextInputEditText etPostSalary = dialogView.findViewById(R.id.etPostSalary);
        TextInputEditText etPostLocation = dialogView.findViewById(R.id.etPostLocation);
        AutoCompleteTextView spPostType = dialogView.findViewById(R.id.spPostType);
        TextInputEditText etPostExperience = dialogView.findViewById(R.id.etPostExperience);
        
        // Validate inputs
        String title = etPostTitle.getText().toString().trim();
        String description = etPostDescription.getText().toString().trim();
        String location = etPostLocation.getText().toString().trim();
        
        if (title.isEmpty()) {
            etPostTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        
        if (description.isEmpty()) {
            etPostDescription.setError("Vui lòng nhập mô tả");
            return;
        }
        
        if (location.isEmpty()) {
            etPostLocation.setError("Vui lòng nhập địa điểm");
            return;
        }
        
        // Get selected post type
        String selectedType = spPostType.getText().toString();
        String postType = "JOB_POSTING";
        if ("Tìm việc".equals(selectedType)) {
            postType = "JOB_SEEKING";
        }
        
        // Format salary
        String salaryInput = etPostSalary.getText().toString().trim();
        String formattedSalary = "Thỏa thuận";
        if (!salaryInput.isEmpty()) {
            try {
                long salaryValue = Long.parseLong(salaryInput);
                java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN"));
                formattedSalary = formatter.format(salaryValue);
            } catch (NumberFormatException e) {
                formattedSalary = salaryInput + " VND";
            }
        }
        
        // Update post entity
        post.setTitle(title);
        post.setDescription(description);
        post.setSalary(formattedSalary);
        post.setLocation(location);
        post.setPostType(postType);
        post.setExperienceLevel(etPostExperience.getText().toString().trim());
        
        // Update in database
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                boolean success = databaseHelper.updateJob(post);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(this, "Cập nhật bài đăng thành công", Toast.LENGTH_SHORT).show();
                        // Just reapply filter to refresh display without changing total count
                        adapter.applyFilter(currentFilter);
                        updateCounts();
                        updateEmptyState();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Cập nhật bài đăng thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("AdminPostManagement", "Error updating post", e);
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi cập nhật bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showPostDetailsDialog(JobEntity post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Create a detailed view for post info
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Get post author info
        UserEntity author = databaseHelper.getUserById(post.getUserId());
        String authorName = author != null ? author.getFullName() : "Không xác định";
        String authorEmail = author != null ? author.getEmail() : "Không có";
        
        // Create text views for post details
        addDetailRow(layout, "👤 Người đăng:", authorName + " (" + authorEmail + ")");
        addDetailRow(layout, "📋 Tiêu đề:", post.getTitle());
        addDetailRow(layout, "📝 Mô tả:", post.getDescription());
        addDetailRow(layout, "💰 Mức lương:", post.getSalary() != null ? post.getSalary() : "Thỏa thuận");
        addDetailRow(layout, "📍 Địa điểm:", post.getLocation());
        addDetailRow(layout, "💼 Loại:", getPostTypeDisplay(post.getPostType()));
        addDetailRow(layout, "⭐ Kinh nghiệm:", post.getExperienceLevel() != null ? post.getExperienceLevel() : "Không yêu cầu");
        addDetailRow(layout, "👥 Số lượng cần:", String.valueOf(post.getNeededAmount()));
        addDetailRow(layout, "⏰ Thời gian làm việc:", post.getWorkingTime() != null ? post.getWorkingTime() : "Chưa xác định");
        
        // Status
        String status = (post.getIsActive() != null && post.getIsActive()) ? "✅ Đang hoạt động" : "🔒 Đã khóa";
        addDetailRow(layout, "📊 Trạng thái:", status);
        
        // Created date
        if (post.getCreatedAt() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
            addDetailRow(layout, "📅 Ngày tạo:", sdf.format(post.getCreatedAt()));
        }
        
        // Location coordinates if available
        if (post.getLocationLatitude() != null && post.getLocationLongitude() != null) {
            addDetailRow(layout, "🗺️ Tọa độ:", 
                String.format("%.6f, %.6f", post.getLocationLatitude(), post.getLocationLongitude()));
        }
        
        builder.setView(layout);
        builder.setTitle("📄 Chi tiết bài đăng");
        builder.setPositiveButton("Đóng", null);
        builder.setNeutralButton("✏️ Chỉnh sửa", (dialog, which) -> {
            showEditPostDialog(post);
        });
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
    
    private String getPostTypeDisplay(String postType) {
        if ("JOB_POSTING".equals(postType)) {
            return "Tuyển dụng";
        } else if ("JOB_SEEKING".equals(postType)) {
            return "Tìm việc";
        }
        return "Không xác định";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_user_management_menu, menu); // Reuse existing menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            loadPosts();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showApplicantsDialog(JobEntity post) {
        progressBar.setVisibility(View.VISIBLE);
        
        executorService.execute(() -> {
            try {
                // Lấy danh sách ứng viên cho job này
                List<com.example.projectprm392.database.ApplicationEntity> applications = 
                    databaseHelper.getApplicationsByJobId(post.getId());
                
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    
                    if (applications.isEmpty()) {
                        // Không có ứng viên
                        new AlertDialog.Builder(this)
                            .setTitle("📝 Danh sách ứng viên")
                            .setMessage("Chưa có ai ứng tuyển vào công việc \"" + post.getTitle() + "\"")
                            .setPositiveButton("OK", null)
                            .show();
                    } else {
                        // Có ứng viên, hiển thị dialog với danh sách
                        showApplicantListDialog(post, applications);
                    }
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải danh sách ứng viên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showApplicantListDialog(JobEntity post, List<com.example.projectprm392.database.ApplicationEntity> applications) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👥 Ứng viên cho: " + post.getTitle());
        
        // Tạo danh sách ứng viên
        StringBuilder message = new StringBuilder();
        message.append("Tổng số ứng viên: ").append(applications.size()).append("\n\n");
        
        for (int i = 0; i < applications.size(); i++) {
            com.example.projectprm392.database.ApplicationEntity app = applications.get(i);
            
            // Lấy thông tin user
            UserEntity user = databaseHelper.getUserById(app.getUserId());
            String userName = user != null ? user.getFullName() : "Không rõ tên";
            String userEmail = user != null ? user.getEmail() : "Không rõ email";
            
            // Trạng thái ứng tuyển
            String status = app.getStatus() != null ? app.getStatus() : "PENDING";
            String statusText;
            switch (status) {
                case "PENDING": statusText = "⏳ Chờ xét duyệt"; break;
                case "ACCEPTED": statusText = "✅ Đã chấp nhận"; break;
                case "REJECTED": statusText = "❌ Đã từ chối"; break;
                default: statusText = "❓ " + status; break;
            }
            
            message.append(String.format("%d. %s\n", i + 1, userName))
                   .append("   📧 ").append(userEmail).append("\n")
                   .append("   📊 ").append(statusText).append("\n");
            
            if (app.getMessage() != null && !app.getMessage().trim().isEmpty()) {
                String shortMessage = app.getMessage().length() > 50 ? 
                    app.getMessage().substring(0, 50) + "..." : app.getMessage();
                message.append("   💬 ").append(shortMessage).append("\n");
            }
            
            message.append("\n");
        }
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("📊 Chi tiết", (dialog, which) -> {
            // Mở activity quản lý ứng tuyển với filter cho job này
            Toast.makeText(this, "Chức năng chi tiết đang phát triển", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Đóng", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Make the dialog scrollable
        dialog.getWindow().setLayout(
            (int) (getResources().getDisplayMetrics().widthPixels * 0.9), 
            (int) (getResources().getDisplayMetrics().heightPixels * 0.7)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
