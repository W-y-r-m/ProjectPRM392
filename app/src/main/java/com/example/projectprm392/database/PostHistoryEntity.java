package com.example.projectprm392.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "post_history")
public class PostHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "date")
    public String date;

    public PostHistoryEntity(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }
}
