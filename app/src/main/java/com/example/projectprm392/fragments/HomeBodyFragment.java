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
import com.example.projectprm392.database.UserEntity;
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
    private MaterialButton btnViewJobHistory;
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
        btnViewJobHistory = view.findViewById(R.id.btnViewJobHistory);
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

        // Xem lịch sử đăng Job
        if (btnViewJobHistory != null) {
            btnViewJobHistory.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), com.example.projectprm392.activities.PostHistoryActivity.class);
                startActivity(intent);
            });
        }

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
        
        android.util.Log.d("HomeBodyFragment", "=== LOADING NEARBY JOBS ===");
        android.util.Log.d("HomeBodyFragment", "Total jobs from database: " + jobs.size());
        
        for (int i = 0; i < jobs.size() && i < 5; i++) {
            JobEntity job = jobs.get(i);
            android.util.Log.d("HomeBodyFragment", "Job " + i + ": " + job.getTitle() + " (ID: " + job.getJobId() + ", Active: " + job.getIsActive() + ")");
        }

        // Sort jobs by distance if user location is available
        jobs = sortJobsByDistance(jobs);

        nearbyJobs.clear();
        nearbyJobs.addAll(jobs);
        nearbyJobsAdapter.updateJobs(nearbyJobs);
        
        android.util.Log.d("HomeBodyFragment", "Updated adapter with " + nearbyJobs.size() + " jobs");

        // Set user location for distance calculation
        if (currentLocation != null) {
            nearbyJobsAdapter.setUserLocation(currentLocation);
        }
    }

    private List<JobEntity> sortJobsByDistance(List<JobEntity> jobs) {
        Location userLoc = getUserLocation();
        if (userLoc == null) {
            return jobs; // Return unsorted if no user location
        }

        // Create a copy of the list and sort by distance
        List<JobEntity> sortedJobs = new ArrayList<>(jobs);
        sortedJobs.sort((job1, job2) -> {
            float distance1 = calculateDistanceToJob(job1, userLoc);
            float distance2 = calculateDistanceToJob(job2, userLoc);
            return Float.compare(distance1, distance2);
        });

        android.util.Log.d("HomeBodyFragment", "Sorted " + sortedJobs.size() + " jobs by distance");
        return sortedJobs;
    }

    private Location getUserLocation() {
        // First try to get current location from GPS
        if (currentLocation != null) {
            android.util.Log.d("HomeBodyFragment", "Using GPS location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
            return currentLocation;
        }

        // Then try to get user's saved location from profile
        try {
            String userId = sessionManager.getUserId();
            android.util.Log.d("HomeBodyFragment", "Getting user location for userId: " + userId);
            if (userId != null) {
                UserEntity user = databaseHelper.getUserById(userId); // Use String method instead of parsing to int
                if (user != null && user.getCurrentLatitude() != null && user.getCurrentLongitude() != null) {
                    Location userProfileLocation = new Location("profile");
                    userProfileLocation.setLatitude(user.getCurrentLatitude());
                    userProfileLocation.setLongitude(user.getCurrentLongitude());
                    android.util.Log.d("HomeBodyFragment", "Using profile location: " + user.getCurrentLatitude() + ", " + user.getCurrentLongitude());
                    return userProfileLocation;
                } else {
                    android.util.Log.d("HomeBodyFragment", "User not found or no coordinates saved");
                }
            }
        } catch (Exception e) {
            android.util.Log.e("HomeBodyFragment", "Error getting user location from profile: " + e.getMessage());
        }

        android.util.Log.d("HomeBodyFragment", "No user location available");
        return null;
    }

    private float calculateDistanceToJob(JobEntity job, Location userLocation) {
        if (job.getLocationLatitude() == null || job.getLocationLongitude() == null) {
            return Float.MAX_VALUE; // Put jobs without location at the end
        }

        // Validate coordinates for Vietnam (rough bounds)
        double jobLat = job.getLocationLatitude();
        double jobLng = job.getLocationLongitude();
        double userLat = userLocation.getLatitude();
        double userLng = userLocation.getLongitude();
        
        // EXPERIMENTAL FIX: Check if coordinates might be swapped
        // Vietnam latitude: 8-24, longitude: 102-110
        boolean jobCoordsSwapped = false;
        boolean userCoordsSwapped = false;
        
        if (jobLat > 50 && jobLng < 30) {
            // Likely swapped - latitude too high, longitude too low
            android.util.Log.w("HomeBodyFragment", "Job coordinates seem swapped! Lat: " + jobLat + ", Lng: " + jobLng + " - trying to fix");
            double temp = jobLat;
            jobLat = jobLng;
            jobLng = temp;
            jobCoordsSwapped = true;
        }
        
        if (userLat > 50 && userLng < 30) {
            // Likely swapped
            android.util.Log.w("HomeBodyFragment", "User coordinates seem swapped! Lat: " + userLat + ", Lng: " + userLng + " - trying to fix");
            double temp = userLat;
            userLat = userLng;
            userLng = temp;
            userCoordsSwapped = true;
        }
        
        // Debug: Test with known coordinates
        // Hanoi: 21.0285, 105.8542
        // Da Nang: 16.0544, 108.2022
        // Expected distance: ~600-700km
        if (Math.abs(jobLat - 21.0285) < 0.01 && Math.abs(jobLng - 105.8542) < 0.01) {
            // This job is in Hanoi
            android.util.Log.d("HomeBodyFragment", "JOB IS IN HANOI - testing distance calculation");
            
            // Test with exact Da Nang coordinates
            Location testDaNangLocation = new Location("test");
            testDaNangLocation.setLatitude(16.0544);
            testDaNangLocation.setLongitude(108.2022);
            
            Location testHanoiLocation = new Location("test");
            testHanoiLocation.setLatitude(21.0285);
            testHanoiLocation.setLongitude(105.8542);
            
            float testDistance = testDaNangLocation.distanceTo(testHanoiLocation) / 1000f;
            android.util.Log.d("HomeBodyFragment", "TEST: Da Nang to Hanoi distance should be ~600-700km, got: " + testDistance + " km");
        }
        
        // Check if coordinates are reasonable for Vietnam
        if (jobLat < 8.0 || jobLat > 24.0 || jobLng < 102.0 || jobLng > 110.0) {
            android.util.Log.w("HomeBodyFragment", "Job coordinates seem invalid: " + jobLat + ", " + jobLng);
        }
        if (userLat < 8.0 || userLat > 24.0 || userLng < 102.0 || userLng > 110.0) {
            android.util.Log.w("HomeBodyFragment", "User coordinates seem invalid: " + userLat + ", " + userLng);
        }

        Location jobLocation = new Location("job");
        jobLocation.setLatitude(jobLat);
        jobLocation.setLongitude(jobLng);
        
        float distanceInMeters = userLocation.distanceTo(jobLocation); // Distance in meters
        float distanceInKm = distanceInMeters / 1000f;
        
        // Debug logging
        android.util.Log.d("HomeBodyFragment", "Calculating distance:");
        android.util.Log.d("HomeBodyFragment", "User location: " + userLat + ", " + userLng + (userCoordsSwapped ? " (FIXED)" : ""));
        android.util.Log.d("HomeBodyFragment", "Job location: " + jobLat + ", " + jobLng + (jobCoordsSwapped ? " (FIXED)" : ""));
        android.util.Log.d("HomeBodyFragment", "Distance: " + distanceInKm + " km");
        
        return distanceInMeters;
    }

    private void loadAllJobs() {
        // Debug coordinates
        databaseHelper.debugCoordinates();
        
        List<JobEntity> jobs = databaseHelper.getAllJobs();
        jobs = sortJobsByDistance(jobs);
        nearbyJobs.clear();
        nearbyJobs.addAll(jobs);
        nearbyJobsAdapter.updateJobs(nearbyJobs);
        
        // Set user location for distance calculation
        if (currentLocation != null) {
            nearbyJobsAdapter.setUserLocation(currentLocation);
        } else {
            Location userLoc = getUserLocation();
            if (userLoc != null) {
                nearbyJobsAdapter.setUserLocation(userLoc);
            }
        }
    }

    private void loadRecommendedJobs() {
        // Load recommended jobs from database (simplified)
        List<JobEntity> jobs = databaseHelper.getNearbyJobs(10); // Get more jobs to sort

        // Sort by distance and take top 5
        jobs = sortJobsByDistance(jobs);
        if (jobs.size() > 5) {
            jobs = jobs.subList(0, 5);
        }

        recommendedJobs.clear();
        recommendedJobs.addAll(jobs);
        recommendedJobsAdapter.updateJobs(recommendedJobs);

        // Set user location for distance calculation
        if (currentLocation != null) {
            recommendedJobsAdapter.setUserLocation(currentLocation);
        }
    }

    public void refreshData() {
        getCurrentLocation();
        loadInitialData();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
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
            case "Gần nhất":
                // Sort all jobs by distance from user
                filteredJobs = sortJobsByDistance(allJobs);
                break;
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
        
        // Sort search results by distance
        searchResults = sortJobsByDistance(searchResults);

        nearbyJobs.clear();
        nearbyJobs.addAll(searchResults);
        nearbyJobsAdapter.updateJobs(nearbyJobs);

        // Set user location for distance calculation
        if (currentLocation != null) {
            nearbyJobsAdapter.setUserLocation(currentLocation);
        } else {
            Location userLoc = getUserLocation();
            if (userLoc != null) {
                nearbyJobsAdapter.setUserLocation(userLoc);
            }
        }

        if (searchResults.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy kết quả cho: " + query, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Tìm thấy " + searchResults.size() + " việc làm", Toast.LENGTH_SHORT).show();
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
