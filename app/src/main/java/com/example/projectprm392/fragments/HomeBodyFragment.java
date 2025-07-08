package com.example.projectprm392.fragments;

import android.Manifest;
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
import com.example.projectprm392.controllers.JobController;
import com.example.projectprm392.models.Job;
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
    private MaterialButton btnShowFilters;
    
    private JobAdapter nearbyJobsAdapter;
    private JobAdapter recommendedJobsAdapter;
    private SessionManager sessionManager;
    private JobController jobController;
    private FusedLocationProviderClient fusedLocationClient;
    
    private List<Job> nearbyJobs = new ArrayList<>();
    private List<Job> recommendedJobs = new ArrayList<>();
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
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters);
        fabPostJob = view.findViewById(R.id.fabPostJob);
        btnShowFilters = view.findViewById(R.id.btnShowFilters);
        
        sessionManager = new SessionManager(requireContext());
        jobController = new JobController(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    private void setupRecyclerViews() {
        // Nearby Jobs RecyclerView
        nearbyJobsAdapter = new JobAdapter(nearbyJobs, this::onJobClick);
        rvNearbyJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNearbyJobs.setAdapter(nearbyJobsAdapter);
        
        // Recommended Jobs RecyclerView
        recommendedJobsAdapter = new JobAdapter(recommendedJobs, this::onJobClick);
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
        fabPostJob.setOnClickListener(v -> handlePostJob());
        
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
        if (currentLocation == null) {
            // Fallback to get all jobs if location is not available
            jobController.getAllJobs(1, new JobController.JobCallback() {
                @Override
                public void onSuccess(List<Job> jobs) {
                    requireActivity().runOnUiThread(() -> {
                        nearbyJobs.clear();
                        nearbyJobs.addAll(jobs);
                        nearbyJobsAdapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi tải việc làm: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }

        // Get nearby jobs based on current location
        jobController.getNearbyJobs(
            currentLocation.getLatitude(),
            currentLocation.getLongitude(),
            10000, // 10km radius
            new JobController.JobCallback() {
                @Override
                public void onSuccess(List<Job> jobs) {
                    requireActivity().runOnUiThread(() -> {
                        nearbyJobs.clear();
                        nearbyJobs.addAll(jobs);
                        nearbyJobsAdapter.notifyDataSetChanged();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi tải việc làm gần bạn: " + error, Toast.LENGTH_SHORT).show();
                        // Fallback to all jobs
                        loadAllJobs();
                    });
                }
            }
        );
    }

    private void loadAllJobs() {
        jobController.getAllJobs(1, new JobController.JobCallback() {
            @Override
            public void onSuccess(List<Job> jobs) {
                requireActivity().runOnUiThread(() -> {
                    nearbyJobs.clear();
                    nearbyJobs.addAll(jobs);
                    nearbyJobsAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi tải danh sách việc làm: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadRecommendedJobs() {
        if (!sessionManager.isLoggedIn()) {
            // If user not logged in, show popular jobs instead
            loadAllJobs();
            return;
        }

        jobController.getRecommendedJobs(new JobController.JobCallback() {
            @Override
            public void onSuccess(List<Job> jobs) {
                requireActivity().runOnUiThread(() -> {
                    recommendedJobs.clear();
                    recommendedJobs.addAll(jobs);
                    recommendedJobsAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Lỗi tải gợi ý việc làm: " + error, Toast.LENGTH_SHORT).show();
                    // Fallback to all jobs for recommended section
                    jobController.getAllJobs(1, new JobController.JobCallback() {
                        @Override
                        public void onSuccess(List<Job> jobs) {
                            requireActivity().runOnUiThread(() -> {
                                recommendedJobs.clear();
                                recommendedJobs.addAll(jobs.subList(0, Math.min(5, jobs.size())));
                                recommendedJobsAdapter.notifyDataSetChanged();
                            });
                        }

                        @Override
                        public void onError(String fallbackError) {
                            // Handle fallback error if needed
                        }
                    });
                });
            }
        });
    }

    private void refreshData() {
        getCurrentLocation();
        loadInitialData();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void handlePostJob() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đăng bài", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // For now, assume users have post quota (this can be improved by fetching from database)
        // TODO: Implement proper quota check by fetching user data from database
        String userRole = sessionManager.getRole();
        if ("EMPLOYER".equals(userRole)) {
            // Navigate to post job activity
            Toast.makeText(requireContext(), "Chuyển đến trang đăng bài", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Chỉ nhà tuyển dụng mới có thể đăng bài", Toast.LENGTH_SHORT).show();
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
        
        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;
        int radius = currentLocation != null ? 10000 : 0;
        
        Double salaryMin = null;
        Double salaryMax = null;
        String salaryUnit = null;
        
        // Apply different filters based on selection
        switch (filter) {
            case "Lương cao":
                salaryMin = 200000.0; // 200k+
                break;
            case "Bán thời gian":
                salaryUnit = "hour";
                break;
            case "Toàn thời gian":
                salaryUnit = "day";
                break;
        }
        
        jobController.searchJobs("", latitude, longitude, radius, salaryMin, salaryMax, salaryUnit,
            new JobController.JobCallback() {
                @Override
                public void onSuccess(List<Job> jobs) {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        nearbyJobs.clear();
                        nearbyJobs.addAll(jobs);
                        nearbyJobsAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Áp dụng filter: " + filter, Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi áp dụng filter: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
    }

    private void onJobClick(Job job) {
        // TODO: Navigate to job details
        Toast.makeText(requireContext(), "Xem chi tiết: " + job.getTitle(), Toast.LENGTH_SHORT).show();
    }

    public void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadInitialData();
            return;
        }

        // Show loading state
        swipeRefreshLayout.setRefreshing(true);

        double latitude = currentLocation != null ? currentLocation.getLatitude() : 0.0;
        double longitude = currentLocation != null ? currentLocation.getLongitude() : 0.0;
        int radius = currentLocation != null ? 10000 : 0; // 10km if location available

        jobController.searchJobs(query, latitude, longitude, radius, null, null, null, 
            new JobController.JobCallback() {
                @Override
                public void onSuccess(List<Job> jobs) {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        nearbyJobs.clear();
                        nearbyJobs.addAll(jobs);
                        nearbyJobsAdapter.notifyDataSetChanged();
                        
                        if (jobs.isEmpty()) {
                            Toast.makeText(requireContext(), "Không tìm thấy kết quả cho: " + query, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi tìm kiếm: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        );
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
}
