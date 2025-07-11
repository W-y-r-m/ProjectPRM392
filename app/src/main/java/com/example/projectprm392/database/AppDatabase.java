package com.example.projectprm392.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {UserEntity.class, JobEntity.class, ApplicationEntity.class, PostHistoryEntity.class}, version = 6, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract JobDao jobDao();
    public abstract ApplicationDao applicationDao();
    public abstract PostHistoryDao postHistoryDao();

    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                            .allowMainThreadQueries() // Chỉ cho development, production nên dùng async
                            .fallbackToDestructiveMigration() // Reset database when schema changes
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
