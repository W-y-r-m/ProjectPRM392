package com.example.projectprm392.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.admin.PaymentManagementAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.PaymentEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.services.QuotaService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminPaymentManagementActivity extends AppCompatActivity 
        implements PaymentManagementAdapter.OnPaymentActionListener {

    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnFilter, btnStats, btnAddTestData;
    private TextView tvPaymentCount, tvFilteredCount, tvRevenue;
    private RecyclerView rvPayments;
    private View layoutEmptyState;
    private androidx.core.widget.ContentLoadingProgressBar progressBar;

    private PaymentManagementAdapter adapter;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;

    private String currentStatusFilter = "ALL";
    private String currentMethodFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment_management);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadPayments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
        btnStats = findViewById(R.id.btnStats);
        btnAddTestData = findViewById(R.id.btnAddTestData);
        tvPaymentCount = findViewById(R.id.tvPaymentCount);
        tvFilteredCount = findViewById(R.id.tvFilteredCount);
        tvRevenue = findViewById(R.id.tvRevenue);
        rvPayments = findViewById(R.id.rvPayments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        progressBar = findViewById(R.id.progressBar);

        databaseHelper = new DatabaseHelper(this);
        executorService = Executors.newCachedThreadPool();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý thanh toán");
        }
    }

    private void setupRecyclerView() {
        adapter = new PaymentManagementAdapter(this, this);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        rvPayments.setAdapter(adapter);
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
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(v -> showFilterDialog());
        btnStats.setOnClickListener(v -> showStatisticsDialog());
        btnAddTestData.setOnClickListener(v -> showAddTestDataDialog());
    }

    private void loadPayments() {
        showLoading(true);
        executorService.execute(() -> {
            try {
                List<PaymentEntity> payments = databaseHelper.getAllPayments();
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.updatePayments(payments);
                    updateCounts();
                    updateEmptyState();
                    updateRevenue();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvPayments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void updateCounts() {
        List<PaymentEntity> allPayments = databaseHelper.getAllPayments();
        List<PaymentEntity> filteredPayments = adapter.getFilteredPayments();
        
        tvPaymentCount.setText("Tổng: " + allPayments.size() + " giao dịch");
        tvFilteredCount.setText("Hiển thị: " + filteredPayments.size());
    }

    private void updateRevenue() {
        executorService.execute(() -> {
            try {
                Long revenue = databaseHelper.getTotalRevenue();
                long totalRevenue = revenue != null ? revenue : 0;
                
                runOnUiThread(() -> {
                    tvRevenue.setText("Doanh thu: " + String.format("%,d VND", totalRevenue));
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvRevenue.setText("Doanh thu: Lỗi tải");
                });
            }
        });
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvPayments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment_filter, null);
        
        AutoCompleteTextView actvStatus = dialogView.findViewById(R.id.actvStatus);
        AutoCompleteTextView actvMethod = dialogView.findViewById(R.id.actvMethod);
        
        // Setup status filter
        String[] statuses = {"Tất cả", "Đang chờ", "Hoàn thành", "Đã hủy", "Thất bại"};
        String[] statusValues = {"ALL", "PENDING", "COMPLETED", "CANCELLED", "FAILED"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses);
        actvStatus.setAdapter(statusAdapter);
        
        // Setup method filter
        String[] methods = {"Tất cả", "Ví MoMo", "Chuyển khoản", "VNPay", "Thẻ tín dụng"};
        String[] methodValues = {"ALL", "MOMO", "BANKING", "VNPAY", "CREDIT_CARD"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, methods);
        actvMethod.setAdapter(methodAdapter);
        
        // Set current values
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(currentStatusFilter)) {
                actvStatus.setText(statuses[i], false);
                break;
            }
        }
        
        for (int i = 0; i < methodValues.length; i++) {
            if (methodValues[i].equals(currentMethodFilter)) {
                actvMethod.setText(methods[i], false);
                break;
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bộ lọc thanh toán")
                .setView(dialogView)
                .setPositiveButton("Áp dụng", (d, which) -> {
                    String selectedStatus = actvStatus.getText().toString();
                    String selectedMethod = actvMethod.getText().toString();
                    
                    // Find corresponding values
                    for (int i = 0; i < statuses.length; i++) {
                        if (statuses[i].equals(selectedStatus)) {
                            currentStatusFilter = statusValues[i];
                            break;
                        }
                    }
                    
                    for (int i = 0; i < methods.length; i++) {
                        if (methods[i].equals(selectedMethod)) {
                            currentMethodFilter = methodValues[i];
                            break;
                        }
                    }
                    
                    adapter.applyFilters(currentStatusFilter, currentMethodFilter);
                    updateCounts();
                    updateEmptyState();
                })
                .setNegativeButton("Đặt lại", (d, which) -> {
                    currentStatusFilter = "ALL";
                    currentMethodFilter = "ALL";
                    adapter.applyFilters(currentStatusFilter, currentMethodFilter);
                    updateCounts();
                    updateEmptyState();
                })
                .create();

        dialog.show();
    }

    private void showStatisticsDialog() {
        executorService.execute(() -> {
            try {
                // Get statistics
                int totalPayments = databaseHelper.getTotalPaymentCount();
                int pendingPayments = databaseHelper.getPaymentCountByStatus("PENDING");
                int completedPayments = databaseHelper.getPaymentCountByStatus("COMPLETED");
                int cancelledPayments = databaseHelper.getPaymentCountByStatus("CANCELLED");
                Long revenue = databaseHelper.getTotalRevenue();
                long totalRevenue = revenue != null ? revenue : 0;
                
                runOnUiThread(() -> {
                    StringBuilder stats = new StringBuilder();
                    stats.append("📊 THỐNG KÊ THANH TOÁN\n\n");
                    stats.append("🔢 Tổng giao dịch: ").append(totalPayments).append("\n");
                    stats.append("⏳ Đang chờ: ").append(pendingPayments).append("\n");
                    stats.append("✅ Hoàn thành: ").append(completedPayments).append("\n");
                    stats.append("❌ Đã hủy: ").append(cancelledPayments).append("\n\n");
                    stats.append("💰 Tổng doanh thu: ").append(String.format("%,d VND", totalRevenue)).append("\n");
                    
                    if (completedPayments > 0) {
                        long avgRevenue = totalRevenue / completedPayments;
                        stats.append("📈 Trung bình/giao dịch: ").append(String.format("%,d VND", avgRevenue));
                    }
                    
                    new AlertDialog.Builder(this)
                            .setTitle("📊 Thống kê thanh toán")
                            .setMessage(stats.toString())
                            .setPositiveButton("Đóng", null)
                            .show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tải thống kê: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAddTestDataDialog() {
        new AlertDialog.Builder(this)
                .setTitle("📝 Tạo dữ liệu test")
                .setMessage("Bạn có muốn tạo dữ liệu test cho quản lý thanh toán không?\n\nSẽ tạo 10 giao dịch mẫu với các trạng thái khác nhau.")
                .setPositiveButton("Tạo", (dialog, which) -> generateTestData())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void generateTestData() {
        executorService.execute(() -> {
            try {
                // Get some users for test data
                List<UserEntity> users = databaseHelper.getAllUsers();
                if (users.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Không có user nào để tạo test data. Vui lòng tạo user trước.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                List<PaymentEntity> testPayments = new ArrayList<>();
                String[] statuses = {"PENDING", "COMPLETED", "CANCELLED", "FAILED"};
                String[] methods = {"MOMO", "BANKING", "VNPAY", "CREDIT_CARD"};
                QuotaService.QuotaPackage[] packages = QuotaService.getAvailablePackages();

                for (int i = 0; i < 10; i++) {
                    UserEntity user = users.get(i % users.size());
                    QuotaService.QuotaPackage pkg = packages[i % packages.length];
                    
                    PaymentEntity payment = new PaymentEntity();
                    payment.setPaymentId("PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    payment.setUserId(user.getId());
                    payment.setUserEmail(user.getEmail());
                    payment.setUserName(user.getFullName());
                    payment.setQuotaAmount(pkg.quotaAmount);
                    payment.setPrice(pkg.price);
                    payment.setPackageName(pkg.name);
                    payment.setPaymentMethod(methods[i % methods.length]);
                    payment.setStatus(statuses[i % statuses.length]);
                    payment.setTransactionId("TXN_" + System.currentTimeMillis() + "_" + i);
                    payment.setNote("Giao dịch test " + (i + 1));
                    payment.setCreatedAt(System.currentTimeMillis() - (i * 3600000)); // 1 hour apart
                    
                    if ("COMPLETED".equals(payment.getStatus())) {
                        payment.setCompletedAt(payment.getCreatedAt() + 300000); // 5 minutes later
                    }
                    
                    testPayments.add(payment);
                }

                // Insert test payments
                for (PaymentEntity payment : testPayments) {
                    databaseHelper.insertPayment(payment);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Đã tạo " + testPayments.size() + " giao dịch test", Toast.LENGTH_SHORT).show();
                    loadPayments();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi tạo test data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // PaymentManagementAdapter.OnPaymentActionListener implementation
    @Override
    public void onPaymentClick(PaymentEntity payment) {
        showPaymentDetailsDialog(payment);
    }

    @Override
    public void onApprovePayment(PaymentEntity payment) {
        showApprovePaymentDialog(payment);
    }

    @Override
    public void onRejectPayment(PaymentEntity payment) {
        showRejectPaymentDialog(payment);
    }

    @Override
    public void onDeletePayment(PaymentEntity payment) {
        showDeletePaymentDialog(payment);
    }

    private void showPaymentDetailsDialog(PaymentEntity payment) {
        StringBuilder details = new StringBuilder();
        details.append("💳 CHI TIẾT GIAO DỊCH\n\n");
        details.append("🆔 Mã GD: ").append(payment.getPaymentId()).append("\n");
        details.append("👤 Người dùng: ").append(payment.getUserName()).append("\n");
        details.append("📧 Email: ").append(payment.getUserEmail()).append("\n");
        details.append("📦 Gói: ").append(payment.getPackageName()).append("\n");
        details.append("🎯 Quota: ").append(payment.getQuotaAmount()).append(" lần đăng\n");
        details.append("💰 Giá: ").append(payment.getFormattedPrice()).append("\n");
        details.append("💳 Phương thức: ").append(payment.getPaymentMethodText()).append("\n");
        details.append("📊 Trạng thái: ").append(payment.getStatusText()).append("\n");
        
        if (payment.getTransactionId() != null) {
            details.append("🔢 Mã giao dịch: ").append(payment.getTransactionId()).append("\n");
        }
        
        details.append("📅 Tạo lúc: ").append(formatTime(payment.getCreatedAt())).append("\n");
        
        if (payment.getCompletedAt() != null) {
            details.append("✅ Hoàn thành: ").append(formatTime(payment.getCompletedAt())).append("\n");
        }
        
        if (payment.getNote() != null && !payment.getNote().isEmpty()) {
            details.append("📝 Ghi chú: ").append(payment.getNote()).append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("💳 Chi tiết giao dịch")
                .setMessage(details.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showApprovePaymentDialog(PaymentEntity payment) {
        if (!"PENDING".equals(payment.getStatus())) {
            Toast.makeText(this, "Chỉ có thể duyệt giao dịch đang chờ", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("✅ Duyệt giao dịch")
                .setMessage("Xác nhận duyệt giao dịch của " + payment.getUserName() + "?\n\n" +
                           "Gói: " + payment.getPackageName() + "\n" +
                           "Quota: " + payment.getQuotaAmount() + " lần đăng\n" +
                           "Giá: " + payment.getFormattedPrice())
                .setPositiveButton("Duyệt", (dialog, which) -> approvePayment(payment))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRejectPaymentDialog(PaymentEntity payment) {
        if (!"PENDING".equals(payment.getStatus())) {
            Toast.makeText(this, "Chỉ có thể từ chối giao dịch đang chờ", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("❌ Từ chối giao dịch")
                .setMessage("Xác nhận từ chối giao dịch của " + payment.getUserName() + "?")
                .setPositiveButton("Từ chối", (dialog, which) -> rejectPayment(payment))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeletePaymentDialog(PaymentEntity payment) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Xóa giao dịch")
                .setMessage("Xác nhận xóa giao dịch này?\n\nHành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> deletePayment(payment))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void approvePayment(PaymentEntity payment) {
        executorService.execute(() -> {
            try {
                // Update payment status
                payment.setStatus("COMPLETED");
                payment.setCompletedAt(System.currentTimeMillis());
                databaseHelper.updatePayment(payment);

                // Add quota to user
                UserEntity user = databaseHelper.getUserById(payment.getUserId());
                if (user != null) {
                    int currentQuota = user.getPostQuota() != null ? user.getPostQuota() : 0;
                    user.setPostQuota(currentQuota + payment.getQuotaAmount());
                    databaseHelper.updateUser(user);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Đã duyệt giao dịch thành công", Toast.LENGTH_SHORT).show();
                    loadPayments();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi duyệt giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void rejectPayment(PaymentEntity payment) {
        executorService.execute(() -> {
            try {
                payment.setStatus("CANCELLED");
                databaseHelper.updatePayment(payment);

                runOnUiThread(() -> {
                    Toast.makeText(this, "❌ Đã từ chối giao dịch", Toast.LENGTH_SHORT).show();
                    loadPayments();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi từ chối giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void deletePayment(PaymentEntity payment) {
        executorService.execute(() -> {
            try {
                databaseHelper.deletePayment(payment);

                runOnUiThread(() -> {
                    Toast.makeText(this, "🗑️ Đã xóa giao dịch", Toast.LENGTH_SHORT).show();
                    loadPayments();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Lỗi xóa giao dịch: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
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
