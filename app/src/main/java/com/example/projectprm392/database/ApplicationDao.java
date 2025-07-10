package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface ApplicationDao {

    // Thêm một application mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ApplicationEntity application);

    // Cập nhật application
    @Update
    void update(ApplicationEntity application);

    // Xoá application 
    @Delete
    void delete(ApplicationEntity application);

    // Lấy tất cả applications
    @Query("SELECT * FROM applications")
    List<ApplicationEntity> getAllApplications();

    // Lấy application theo id
    @Query("SELECT * FROM applications WHERE id = :id")
    ApplicationEntity getApplicationById(int id);

    // Lấy application theo userId
    @Query("SELECT * FROM applications WHERE userId = :userId")
    List<ApplicationEntity> getApplicationsByUserId(int userId);

    // Lấy application theo jobId
    @Query("SELECT * FROM applications WHERE jobId = :jobId")
    List<ApplicationEntity> getApplicationsByJobId(int jobId);

    // Lấy application theo status
    @Query("SELECT * FROM applications WHERE status = :status")
    List<ApplicationEntity> getApplicationsByStatus(String status);
}
