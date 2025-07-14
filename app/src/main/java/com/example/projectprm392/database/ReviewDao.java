package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insertReview(ReviewEntity review);

    @Query("SELECT * FROM reviews WHERE jobId = :jobId")
    List<ReviewEntity> getReviewsForJob(String jobId);
}
