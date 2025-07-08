package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JobDao {
    @Query("SELECT * FROM jobs WHERE is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getAllActiveJobs();
    
    @Query("SELECT * FROM jobs WHERE id = :id")
    JobEntity getById(int id);
    
    @Query("SELECT * FROM jobs WHERE job_id = :jobId")
    JobEntity getByJobId(String jobId);
    
    @Query("SELECT * FROM jobs WHERE user_id = :userId AND is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getJobsByUserId(int userId);
    
    @Query("SELECT * FROM jobs WHERE title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%' AND is_active = 1")
    List<JobEntity> searchJobs(String keyword);
    
    @Query("SELECT * FROM jobs WHERE job_type = :jobType AND is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getJobsByType(String jobType);
    
    @Query("SELECT * FROM jobs WHERE experience_level = :experienceLevel AND is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getJobsByExperienceLevel(String experienceLevel);
    
    @Query("SELECT * FROM jobs WHERE location LIKE '%' || :location || '%' AND is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getJobsByLocation(String location);
    
    @Query("SELECT * FROM jobs WHERE status = :status AND is_active = 1 ORDER BY created_at DESC")
    List<JobEntity> getJobsByStatus(String status);
    
    // Tìm việc làm gần vị trí hiện tại (tính toán khoảng cách đơn giản)
    @Query("SELECT * FROM jobs WHERE is_active = 1 AND location_latitude IS NOT NULL AND location_longitude IS NOT NULL ORDER BY created_at DESC LIMIT :limit")
    List<JobEntity> getNearbyJobs(int limit);
    
    @Query("SELECT COUNT(*) FROM jobs WHERE user_id = :userId AND is_active = 1")
    int countJobsByUserId(int userId);
    
    @Insert
    long insert(JobEntity job);
    
    @Update
    void update(JobEntity job);
    
    @Delete
    void delete(JobEntity job);
    
    @Query("UPDATE jobs SET is_active = 0 WHERE id = :id")
    void deactivateJob(int id);
    
    @Query("DELETE FROM jobs")
    void deleteAll();
}
