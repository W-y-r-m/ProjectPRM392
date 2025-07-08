package com.example.projectprm392.database;

import android.content.Context;

import com.example.projectprm392.models.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {
    private AppDatabase database;
    private UserDao userDao;
    private ExecutorService executor;
    
    public DatabaseHelper(Context context) {
        database = AppDatabase.getDatabase(context);
        userDao = database.userDao();
        executor = Executors.newFixedThreadPool(4);
    }
    
    // User methods
    public UserEntity getUserByEmail(String email) {
        return userDao.getByEmail(email);
    }
    
    public UserEntity getUserByEmailAndPassword(String email, String password) {
        return userDao.getByEmailAndPassword(email, password);
    }
    
    public boolean isEmailExists(String email) {
        return userDao.countByEmail(email) > 0;
    }
    
    public long insertUser(User user) {
        UserEntity entity = convertUserToEntity(user);
        return userDao.insert(entity);
    }
    
    public void updateUser(User user) {
        UserEntity entity = convertUserToEntity(user);
        // Cần có ID để update
        UserEntity existingEntity = userDao.getByEmail(user.getEmail());
        if (existingEntity != null) {
            entity.setId(existingEntity.getId());
            userDao.update(entity);
        }
    }
    
    public void deleteUser(String email) {
        UserEntity entity = userDao.getByEmail(email);
        if (entity != null) {
            userDao.delete(entity);
        }
    }
    
    public List<UserEntity> getAllUsers() {
        return userDao.getAll();
    }
    
    public List<UserEntity> getUsersByRole(String role) {
        return userDao.getByRole(role);
    }
    
    // Convert methods
    public UserEntity convertUserToEntity(User user) {
        if (user == null) return null;
        
        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId() != null ? user.getUserId().toString() : UUID.randomUUID().toString());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setGender(user.getGender());
        entity.setRole(user.getRole());
        entity.setLevelOfViolation(user.getLevelOfViolation());
        entity.setDescription(user.getDescription());
        entity.setPostQuota(user.getPostQuota());
        entity.setCurrentLatitude(user.getCurrentLatitude());
        entity.setCurrentLongitude(user.getCurrentLongitude());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setIsActive(user.getIsActive());
        
        return entity;
    }
    
    public User convertEntityToUser(UserEntity entity) {
        if (entity == null) return null;
        
        User user = new User();
        user.setUserId(UUID.fromString(entity.getUserId()));
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setFullName(entity.getFullName());
        user.setGender(entity.getGender());
        user.setRole(entity.getRole());
        user.setLevelOfViolation(entity.getLevelOfViolation());
        user.setDescription(entity.getDescription());
        user.setPostQuota(entity.getPostQuota());
        user.setCurrentLatitude(entity.getCurrentLatitude());
        user.setCurrentLongitude(entity.getCurrentLongitude());
        user.setCreatedAt(entity.getCreatedAt());
        user.setIsActive(entity.getIsActive());
        
        return user;
    }
    
    // Initialize với dữ liệu mẫu
    public void initializeSampleData() {
        // Kiểm tra xem có data chưa
        if (userDao.getAll().isEmpty()) {
            // Tạo user mẫu
            User testUser = new User("test@gmail.com", "123456", "Nguyen Van A", "worker");
            testUser.setGender(true);
            testUser.setDescription("Test user");
            testUser.setCreatedAt(new Date());
            
            User employerUser = new User("employer@gmail.com", "123456", "Tran Thi B", "employer");
            employerUser.setGender(false);
            employerUser.setDescription("Test employer");
            employerUser.setCreatedAt(new Date());
            
            insertUser(testUser);
            insertUser(employerUser);
        }
    }
    
    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
