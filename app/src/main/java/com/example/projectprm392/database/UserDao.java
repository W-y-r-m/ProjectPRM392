package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface UserDao {
    
    @Query("SELECT * FROM users")
    List<UserEntity> getAll();
    
    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(int id);
    
    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    UserEntity getByUserId(String userId);
    
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
    
    // Verification methods
    @Query("UPDATE users SET verification_code = :code, verification_code_expires_at = :expiresAt WHERE email = :email")
    void updateVerificationCode(String email, String code, Date expiresAt);
    
    @Query("UPDATE users SET is_verified = 1, verification_code = NULL, verification_code_expires_at = NULL WHERE email = :email AND verification_code = :code AND verification_code_expires_at > :currentTime")
    int verifyUser(String email, String code, Date currentTime);
    
    @Query("SELECT * FROM users WHERE email = :email AND verification_code = :code AND verification_code_expires_at > :currentTime")
    UserEntity findUserByVerificationCode(String email, String code, Date currentTime);
    
    // Password update method
    @Query("UPDATE users SET password = :hashedPassword WHERE email = :email")
    int updatePassword(String email, String hashedPassword);
    
    // Clear verification code
    @Query("UPDATE users SET verification_code = NULL, verification_code_expires_at = NULL WHERE email = :email")
    void clearVerificationCode(String email);
}
