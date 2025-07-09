package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PostHistoryDao {
    @Insert
    void insert(PostHistoryEntity postHistory);

    @Query("SELECT * FROM post_history ORDER BY date DESC")
    List<PostHistoryEntity> getAllPostHistory();
}
