package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReportDao {
    @Insert
    void insert(ReportEntity report);

    @Update
    void update(ReportEntity report);

    @Query("DELETE FROM reports WHERE ReportId = :reportId")
    void deleteById(String reportId);

    @Query("SELECT * FROM reports ORDER BY CreatedAt DESC")
    List<ReportEntity> getAllReports();

    @Query("SELECT * FROM reports WHERE ReportId = :reportId")
    ReportEntity getReportById(String reportId);

    @Query("SELECT * FROM reports WHERE Status = :status ORDER BY CreatedAt DESC")
    List<ReportEntity> getReportsByStatus(String status);
}
