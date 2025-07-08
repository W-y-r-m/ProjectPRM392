package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users")
    List<UserEntity> getAll();
    
    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(int id);
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);
    
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    UserEntity getByEmailAndPassword(String email, String password);
    
    @Query("SELECT * FROM users WHERE role = :role")
    List<UserEntity> getByRole(String role);
    
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int countByEmail(String email);
    
    @Insert
    long insert(UserEntity user);
    
    @Update
    void update(UserEntity user);
    
    @Delete
    void delete(UserEntity user);
    
    @Query("DELETE FROM users WHERE id = :id")
    void deleteById(int id);
    
    @Query("DELETE FROM users")
    void deleteAll();
}
