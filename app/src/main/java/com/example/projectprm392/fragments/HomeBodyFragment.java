package com.example.projectprm392.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.JobAdapter;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeBodyFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvNearbyJobs;
    private RecyclerView rvRecommendedJobs;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabPostJob;
    private MaterialButton btnPostJob;
    private MaterialButton btnShowFilters;

    private JobAdapter nearbyJobsAdapter;
    private JobAdapter recommendedJobsAdapter;
    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private FusedLocationProviderClient fusedLocationClient;

    private List<JobEntity> nearbyJobs = new ArrayList<>();
    private List<JobEntity> recommendedJobs = new ArrayList<>();
    private Location currentLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_body, container, false);

        initViews(view);
        setupRecyclerViews();
        setupListeners();
        setupLocation();
        loadInitialData();

        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvNearbyJobs = view.findViewById(R.id.rvNearbyJobs);
        rvRecommendedJobs = view.findViewById(R.id.rvRecommendedJobs);
        chipGroupFilters = view.findViewById(R.id.chipGroupJobType);
        fabPostJob = view.findViewById(R.id.fabPostJob);
        btnPostJob = view.findViewById(R.id.btnPostJob);
        btnShowFilters = view.findViewById(R.id.btnApplyFilters);

        // Debug log
        android.util.Log.d("HomeBodyFragment", "fabPostJob: " + (fabPostJob != null ? "found" : "null"));
        android.util.Log.d("HomeBodyFragment", "btnPostJob: " + (btnPostJob != null ? "found" : "null"));

        sessionManager = new SessionManager(requireContext());
        databaseHelper = new DatabaseHelper(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize sample data
        databaseHelper.initializeSampleData();
    }

    private void setupRecyclerViews() {
        // Nearby Jobs RecyclerView
        nearbyJobsAdapter = new JobAdapter(nearbyJobs, new JobAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(JobEntity job) {
                // Navigate to JobDetailActivity
                Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.JobDetailActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                startActivity(intent);
            }

            @Override
            public void onContactClick(JobEntity job) {
                // Handle contact click for job seeking posts
                if ("JOB_SEEKING".equals(job.getPostType())) {
                    Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.JobDetailActivity.class);
                    intent.putExtra("JOB_ID", job.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Liên hệ: " + job.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSaveClick(JobEntity job) {
                Toast.makeText(requireContext(), "Đã lưu: " + job.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }, databaseHelper);
        rvNearbyJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNearbyJobs.setAdapter(nearbyJobsAdapter);

        // Recommended Jobs RecyclerView
        recommendedJobsAdapter = new JobAdapter(recommendedJobs, new JobAdapter.OnJobClickListener() {
            @Override
            public void onJobClick(JobEntity job) {
                // Navigate to JobDetailActivity
                Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.JobDetailActivity.class);
                intent.putExtra("JOB_ID", job.getId());
                startActivity(intent);
            }

            @Override
            public void onContactClick(JobEntity job) {
                // Handle contact click for job seeking posts
                if ("JOB_SEEKING".equals(job.getPostType())) {
                    Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.JobDetailActivity.class);
                    intent.putExtra("JOB_ID", job.getId());
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Liên hệ: " + job.getTitle(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSaveClick(JobEntity job) {
                Toast.makeText(requireContext(), "Đã lưu: " + job.getTitle(), Toast.LENGTH_SHORT).show();
            }
        }, databaseHelper);

        rvRecommendedJobs.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvRecommendedJobs.setAdapter(recommendedJobsAdapter);

        // Setup nested scrolling
        rvNearbyJobs.setNestedScrollingEnabled(false);
        rvRecommendedJobs.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        // Post job button
        if (fabPostJob != null) {
            fabPostJob.setOnClickListener(v -> handlePostJob());
        }
        if (btnPostJob != null) {
            btnPostJob.setOnClickListener(v -> handlePostJob());
        }

        // Filter button
        btnShowFilters.setOnClickListener(v -> showFilterDialog());

        // Quick filters
        setupQuickFilters();
    }

    private void setupQuickFilters() {
        String[] filters = {"Gần nhất", "Lương cao", "Mới nhất", "Bán thời gian", "Toàn thời gian"};

        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    applyQuickFilter(filter);
                }
            });
            chipGroupFilters.addView(chip);
        }
    }

    private void setupLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        getCurrentLocation();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        // Update adapters with location for distance calculation
                        nearbyJobsAdapter.setUserLocation(location);
                        recommendedJobsAdapter.setUserLocation(location);
                        loadNearbyJobs();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                    // Load jobs without location
                    loadNearbyJobs();
                });
    }

    private void loadInitialData() {
        loadNearbyJobs();
        loadRecommendedJobs();
    }

    private void loadNearbyJobs() {
        // Load jobs from database
        List<JobEntity> jobs = databaseHelper.getAllJobs();

        nearbyJobs.clear();
        nearbyJobs.addAll(jobs);
        nearbyJobsAdapter.updateJobs(nearbyJobs);

        // Set user location for distance calculation
        if (currentLocation != null) {
            nearbyJobsAdapter.setUserLocation(currentLocation);
        }
    }

    private void loadAllJobs() {
        List<JobEntity> jobs = databaseHelper.getAllJobs();
        nearbyJobs.clear();
        nearbyJobs.addAll(jobs);
        nearbyJobsAdapter.updateJobs(nearbyJobs);
    }

    private void loadRecommendedJobs() {
        // Load recommended jobs from database (simplified)
        List<JobEntity> jobs = databaseHelper.getNearbyJobs(5); // Get top 5 jobs

        recommendedJobs.clear();
        recommendedJobs.addAll(jobs);
        recommendedJobsAdapter.updateJobs(recommendedJobs);

        // Set user location for distance calculation
        if (currentLocation != null) {
            recommendedJobsAdapter.setUserLocation(currentLocation);
        }
    }

    private void refreshData() {
        getCurrentLocation();
        loadInitialData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void handlePostJob() {
        Toast.makeText(requireContext(), "Nút đăng tin được nhấn", Toast.LENGTH_SHORT).show();
        
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Navigate to post type selection activity
            Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.PostTypeSelectionActivity.class);
            startActivityForResult(intent, 1001);
            Toast.makeText(requireContext(), "Đang chuyển trang...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi chuyển trang: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void showBuyQuotaDialog() {
        // TODO: Implement buy quota dialog
        Toast.makeText(requireContext(), "Bạn đã hết lượt đăng bài. Vui lòng mua thêm!", Toast.LENGTH_LONG).show();
    }

    private void showFilterDialog() {
        // TODO: Implement advanced filter dialog
        Toast.makeText(requireContext(), "Bộ lọc nâng cao", Toast.LENGTH_SHORT).show();
    }

    private void applyQuickFilter(String filter) {
        // Show loading state
        swipeRefreshLayout.setRefreshing(true);

        List<JobEntity> filteredJobs = new ArrayList<>();
        List<JobEntity> allJobs = databaseHelper.getAllJobs();
        switch (filter) {
            case "Lương cao":
                for (JobEntity job : allJobs) {
                    try {
                        String salaryStr = job.getSalary().replaceAll("[^0-9]", "");
                        if (!salaryStr.isEmpty() && Integer.parseInt(salaryStr) >= 200000) {
                            filteredJobs.add(job);
                        }
                    } catch (Exception ignored) {}
                }
                break;
            case "Bán thời gian":
                for (JobEntity job : allJobs) {
                    if (job.getJobType() != null && job.getJobType().toLowerCase().contains("part")) {
                        filteredJobs.add(job);
                    }
                }
                break;
            case "Toàn thời gian":
                for (JobEntity job : allJobs) {
                    if (job.getJobType() != null && job.getJobType().toLowerCase().contains("full")) {
                        filteredJobs.add(job);
                    }
                }
                break;
            case "Mới nhất":
                filteredJobs.addAll(allJobs);
                break;
            case "Gần nhất":
                filteredJobs.addAll(databaseHelper.getNearbyJobs(10));
                break;
            default:
                filteredJobs.addAll(allJobs);
        }
        nearbyJobs.clear();
        nearbyJobs.addAll(filteredJobs);
        nearbyJobsAdapter.updateJobs(nearbyJobs);
        swipeRefreshLayout.setRefreshing(false);
        Toast.makeText(requireContext(), "Áp dụng filter: " + filter, Toast.LENGTH_SHORT).show();
    }

    public void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadInitialData();
            return;
        }

        // Search jobs from database
        List<JobEntity> searchResults = databaseHelper.searchJobs(query);

        nearbyJobs.clear();
        nearbyJobs.addAll(searchResults);
        nearbyJobsAdapter.updateJobs(nearbyJobs);

        if (searchResults.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy kết quả cho: " + query, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Cần quyền truy cập vị trí để hiển thị việc làm gần bạn", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK) {
            // Refresh data after successful post creation
            refreshData();
            Toast.makeText(requireContext(), "Bài đăng đã được tạo thành công!", Toast.LENGTH_SHORT).show();
        }
    }
}
