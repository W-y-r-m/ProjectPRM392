package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class ReviewEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public int jobId;
    public String comment;
    public float rating;

    public ReviewEntity(int userId, int jobId, String comment, float rating) {
        this.userId = userId;
        this.jobId = jobId;
        this.comment = comment;
        this.rating = rating;
    }
}
