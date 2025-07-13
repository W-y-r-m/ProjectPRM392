package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReportDao {
    @Insert
    void insert(ReportEntity report);

    @Query("SELECT * FROM reports ORDER BY CreatedAt DESC")
    List<ReportEntity> getAllReports();
}
