package com.example.projectprm392.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.models.Job;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobController {
    private static final String TAG = "JobController";
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Context context;
    private ExecutorService executor;
    private Handler mainHandler;

    public JobController(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.sessionManager = new SessionManager(context);
        this.executor = Executors.newFixedThreadPool(4);
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize with some mock data if database is empty
        initializeMockData();
    }

    public interface JobCallback {
        void onSuccess(List<Job> jobs);
        void onError(String error);
    }

    private void initializeMockData() {
        executor.execute(() -> {
            try {
                // Check if jobs already exist
                List<Job> existingJobs = getAllJobsFromDb();
                if (existingJobs.isEmpty()) {
                    // Create some mock jobs
                    createMockJobs();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing mock data", e);
            }
        });
    }

    private void createMockJobs() {
        List<Job> mockJobs = new ArrayList<>();
        
        // Create 10 mock jobs
        for (int i = 1; i <= 10; i++) {
            Job job = new Job();
            job.setJobId(UUID.randomUUID());
            job.setUserId(UUID.randomUUID()); // Mock employer
            job.setTitle("Công việc " + i);
            job.setDescription("Mô tả chi tiết cho công việc số " + i + ". Yêu cầu kinh nghiệm và kỹ năng phù hợp.");
            job.setSalary(BigDecimal.valueOf(100000 + i * 25000));
            job.setSalaryUnit(i % 2 == 0 ? Job.SalaryUnit.HOUR : Job.SalaryUnit.DAY);
            job.setNeededAmount(2 + (i % 3));
            job.setLocationLatitude(10.7769 + (Math.random() - 0.5) * 0.1); // Around Ho Chi Minh City
            job.setLocationLongitude(106.7009 + (Math.random() - 0.5) * 0.1);
            job.setStatus(Job.JobStatus.OPEN);
            mockJobs.add(job);
        }
        
        // Save to database (you'll need to implement this in DatabaseHelper)
        for (Job job : mockJobs) {
            // TODO: Implement saveJob in DatabaseHelper
            // databaseHelper.insertJob(job);
        }
    }

    public void getNearbyJobs(double latitude, double longitude, int radius, JobCallback callback) {
        executor.execute(() -> {
            try {
                List<Job> allJobs = getAllJobsFromDb();
                List<Job> nearbyJobs = new ArrayList<>();
                
                for (Job job : allJobs) {
                    if (job.getLocationLatitude() != null && job.getLocationLongitude() != null) {
                        double distance = calculateDistance(latitude, longitude, 
                            job.getLocationLatitude(), job.getLocationLongitude());
                        if (distance <= radius) {
                            nearbyJobs.add(job);
                        }
                    }
                }
                
                mainHandler.post(() -> callback.onSuccess(nearbyJobs));
            } catch (Exception e) {
                Log.e(TAG, "Error getting nearby jobs", e);
                mainHandler.post(() -> callback.onError("Lỗi tải danh sách việc làm gần bạn"));
            }
        });
    }

    public void getRecommendedJobs(JobCallback callback) {
        executor.execute(() -> {
            try {
                List<Job> allJobs = getAllJobsFromDb();
                // For now, just return first 5 jobs as "recommended"
                List<Job> recommendedJobs = allJobs.subList(0, Math.min(5, allJobs.size()));
                
                mainHandler.post(() -> callback.onSuccess(recommendedJobs));
            } catch (Exception e) {
                Log.e(TAG, "Error getting recommended jobs", e);
                mainHandler.post(() -> callback.onError("Lỗi tải gợi ý việc làm"));
            }
        });
    }

    public void searchJobs(String query, double latitude, double longitude, int radius, 
                          Double salaryMin, Double salaryMax, String salaryUnit, JobCallback callback) {
        executor.execute(() -> {
            try {
                List<Job> allJobs = getAllJobsFromDb();
                List<Job> filteredJobs = new ArrayList<>();
                
                for (Job job : allJobs) {
                    boolean matches = true;
                    
                    // Text search
                    if (query != null && !query.trim().isEmpty()) {
                        String searchQuery = query.toLowerCase();
                        if (!job.getTitle().toLowerCase().contains(searchQuery) && 
                            !job.getDescription().toLowerCase().contains(searchQuery)) {
                            matches = false;
                        }
                    }
                    
                    // Salary filter
                    if (salaryMin != null && job.getSalary().doubleValue() < salaryMin) {
                        matches = false;
                    }
                    if (salaryMax != null && job.getSalary().doubleValue() > salaryMax) {
                        matches = false;
                    }
                    
                    // Salary unit filter
                    if (salaryUnit != null && !job.getSalaryUnit().getValue().equals(salaryUnit)) {
                        matches = false;
                    }
                    
                    // Location filter
                    if (radius > 0 && job.getLocationLatitude() != null && job.getLocationLongitude() != null) {
                        double distance = calculateDistance(latitude, longitude, 
                            job.getLocationLatitude(), job.getLocationLongitude());
                        if (distance > radius) {
                            matches = false;
                        }
                    }
                    
                    if (matches) {
                        filteredJobs.add(job);
                    }
                }
                
                mainHandler.post(() -> callback.onSuccess(filteredJobs));
            } catch (Exception e) {
                Log.e(TAG, "Error searching jobs", e);
                mainHandler.post(() -> callback.onError("Lỗi tìm kiếm"));
            }
        });
    }

    public void getAllJobs(int page, JobCallback callback) {
        executor.execute(() -> {
            try {
                List<Job> allJobs = getAllJobsFromDb();
                mainHandler.post(() -> callback.onSuccess(allJobs));
            } catch (Exception e) {
                Log.e(TAG, "Error getting all jobs", e);
                mainHandler.post(() -> callback.onError("Lỗi tải danh sách việc làm"));
            }
        });
    }

    private List<Job> getAllJobsFromDb() {
        // TODO: Implement this in DatabaseHelper
        // For now, return mock data
        List<Job> jobs = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            Job job = new Job();
            job.setJobId(UUID.randomUUID());
            job.setUserId(UUID.randomUUID());
            job.setTitle("Công việc " + i);
            job.setDescription("Mô tả chi tiết cho công việc số " + i);
            job.setSalary(BigDecimal.valueOf(100000 + i * 25000));
            job.setSalaryUnit(i % 2 == 0 ? Job.SalaryUnit.HOUR : Job.SalaryUnit.DAY);
            job.setNeededAmount(2 + (i % 3));
            job.setLocationLatitude(10.7769 + (Math.random() - 0.5) * 0.1);
            job.setLocationLongitude(106.7009 + (Math.random() - 0.5) * 0.1);
            job.setStatus(Job.JobStatus.OPEN);
            jobs.add(job);
        }
        
        return jobs;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        
        return distance;
    }
}
