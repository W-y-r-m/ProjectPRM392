package com.example.projectprm392.api;

import com.example.projectprm392.models.Job;
import com.example.projectprm392.viewmodels.JobResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface JobApiService {
    
    @GET("jobs/nearby")
    Call<JobResponse<List<Job>>> getNearbyJobs(
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("radius") int radius,
        @Query("limit") int limit
    );
    
    @GET("jobs/recommended")
    Call<JobResponse<List<Job>>> getRecommendedJobs(
        @Query("userId") String userId,
        @Query("limit") int limit
    );
    
    @GET("jobs/search")
    Call<JobResponse<List<Job>>> searchJobs(
        @Query("query") String query,
        @Query("latitude") double latitude,
        @Query("longitude") double longitude,
        @Query("radius") int radius,
        @Query("salaryMin") Double salaryMin,
        @Query("salaryMax") Double salaryMax,
        @Query("salaryUnit") String salaryUnit,
        @Query("limit") int limit
    );
    
    @GET("jobs")
    Call<JobResponse<List<Job>>> getAllJobs(
        @Query("page") int page,
        @Query("limit") int limit
    );
}
