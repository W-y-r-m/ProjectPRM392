package com.example.projectprm392.database;

import android.content.Context;
import android.util.Log;

import com.example.projectprm392.models.User;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private AppDatabase database;
    private UserDao userDao;
    private JobDao jobDao;
    private ApplicationDao applicationDao;
    private ReportDao reportDao;
    private ChatDao chatDao;
    private ExecutorService executor;

    public DatabaseHelper(Context context) {
        database = AppDatabase.getDatabase(context);
        userDao = database.userDao();
        jobDao = database.jobDao();
        applicationDao = database.applicationDao();
        reportDao = database.reportDao();
        chatDao = database.chatDao();
        executor = Executors.newFixedThreadPool(4);
    }

    // User methods
    public UserEntity getUserById(int userId) {
        return userDao.getById(userId);
    }

    public UserEntity getUserById(String userId) {
        return userDao.getByUserId(userId);
    }

    public UserEntity getUserByEmail(String email) {
        return userDao.getByEmail(email);
    }

    public UserEntity getUserByEmailAndPassword(String email, String password) {
        android.util.Log.d("DatabaseHelper", "Login attempt for email: " + email + " with password: " + password);
        UserEntity result = userDao.getByEmailAndPassword(email, password);
        android.util.Log.d("DatabaseHelper", "Login result: " + (result != null ? "SUCCESS" : "FAILED"));
        return result;
    }

    public boolean isEmailExists(String email) {
        return userDao.countByEmail(email) > 0;
    }

    public long insertUser(User user) {
        UserEntity entity = convertUserToEntity(user);
        return userDao.insert(entity);
    }

    public boolean updateUser(User user) {
        UserEntity entity = convertUserToEntity(user);
        // Cần có ID để update
        UserEntity existingEntity = userDao.getByUserId(user.getUserId().toString());
        if (existingEntity != null) {
            entity.setId(existingEntity.getId());
            userDao.update(entity);
            return true;
        }
        return false;
    }

    public boolean updateUser(UserEntity userEntity) {
        Log.d(TAG, "=== UPDATE USER ENTITY DEBUG START ===");
        Log.d(TAG, "Updating UserEntity: " + userEntity.getEmail());
        Log.d(TAG, "Full Name: " + userEntity.getFullName());
        Log.d(TAG, "Description: " + userEntity.getDescription());
        Log.d(TAG, "Role: " + userEntity.getRole());

        try {
            // Cần có ID để update, nếu ID = 0 thì tìm theo email
            if (userEntity.getId() == 0) {
                UserEntity existingEntity = userDao.getByEmail(userEntity.getEmail());
                if (existingEntity != null) {
                    userEntity.setId(existingEntity.getId());
                    Log.d(TAG, "Found existing entity with DB ID: " + existingEntity.getId());
                } else {
                    Log.e(TAG, "UserEntity not found for update: " + userEntity.getEmail());
                    Log.d(TAG, "=== UPDATE USER ENTITY DEBUG END ===");
                    return false;
                }
            }

            Log.d(TAG, "Calling userDao.update with ID: " + userEntity.getId());
            Log.d(TAG, "Data to update - Full Name: " + userEntity.getFullName());
            Log.d(TAG, "Data to update - Description: " + userEntity.getDescription());
            Log.d(TAG, "Data to update - Role: " + userEntity.getRole());

            userDao.update(userEntity);

            // Verify update
            UserEntity updatedEntity = userDao.getByEmail(userEntity.getEmail());
            Log.d(TAG, "After update - Full Name: " + updatedEntity.getFullName());
            Log.d(TAG, "After update - Description: " + updatedEntity.getDescription());
            Log.d(TAG, "After update - Role: " + updatedEntity.getRole());
            Log.d(TAG, "=== UPDATE USER ENTITY DEBUG END ===");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating UserEntity: " + e.getMessage(), e);
            Log.d(TAG, "=== UPDATE USER ENTITY DEBUG END ===");
            return false;
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

    /**
     * Update password for user by email
     */
    public boolean updatePassword(String email, String hashedPassword) {
        try {
            Log.d(TAG, "=== UPDATE PASSWORD DEBUG START ===");
            Log.d(TAG, "Updating password for email: " + email);
            Log.d(TAG, "New password length: " + hashedPassword.length());

            int rowsAffected = userDao.updatePassword(email, hashedPassword);
            Log.d(TAG, "Rows affected: " + rowsAffected);

            // Verify password was updated
            UserEntity user = userDao.getByEmail(email);
            if (user != null) {
                Log.d(TAG, "User found after update, password length: " + user.getPassword().length());
                Log.d(TAG, "Password matches: " + hashedPassword.equals(user.getPassword()));
            } else {
                Log.e(TAG, "User not found after password update!");
            }

            Log.d(TAG, "=== UPDATE PASSWORD DEBUG END ===");
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
            e.printStackTrace();
            return false;
        }
    }

    // ========== JOB MANAGEMENT METHODS ==========

    public List<JobEntity> getAllJobs() {
        return jobDao.getAllActiveJobs();
    }

    public List<JobEntity> getAllJobsIncludingInactive() {
        return jobDao.getAll();
    }

    public JobEntity getJobById(int id) {
        return jobDao.getById(id);
    }

    public JobEntity getJobByJobId(String jobId) {
        return jobDao.getByJobId(jobId);
    }

    public List<JobEntity> getJobsByUserId(int userId) {
        return jobDao.getJobsByUserId(userId);
    }

    public List<JobEntity> searchJobs(String keyword) {
        return jobDao.searchJobs(keyword);
    }

    public List<JobEntity> getJobsByType(String jobType) {
        return jobDao.getJobsByType(jobType);
    }

    public List<JobEntity> getJobsByStatus(String status) {
        return jobDao.getJobsByStatus(status);
    }

    public boolean insertJob(JobEntity job) {
        try {
            Log.d(TAG, "Inserting job: " + job.getTitle());
            long result = jobDao.insert(job);
            Log.d(TAG, "Job insert result: " + result);
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting job", e);
            return false;
        }
    }

    public boolean updateJob(JobEntity job) {
        try {
            Log.d(TAG, "=== UPDATE JOB DEBUG START ===");
            Log.d(TAG, "Updating job: " + job.getTitle());
            Log.d(TAG, "Job ID: " + job.getId());
            Log.d(TAG, "Active status: " + job.getIsActive());

            jobDao.update(job);

            // Verify update
            JobEntity updatedJob = jobDao.getById(job.getId());
            if (updatedJob != null) {
                Log.d(TAG, "After update - Active status: " + updatedJob.getIsActive());
            }

            Log.d(TAG, "=== UPDATE JOB DEBUG END ===");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating job", e);
            return false;
        }
    }

    public boolean deleteJob(int jobId) {
        try {
            Log.d(TAG, "Deleting job with ID: " + jobId);
            JobEntity job = jobDao.getById(jobId);
            if (job != null) {
                jobDao.delete(job);
                Log.d(TAG, "Job deleted successfully");
                return true;
            } else {
                Log.e(TAG, "Job not found for deletion");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting job", e);
            return false;
        }
    }

    public boolean deactivateJob(int jobId) {
        try {
            Log.d(TAG, "Deactivating job with ID: " + jobId);
            jobDao.deactivateJob(jobId);
            Log.d(TAG, "Job deactivated successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error deactivating job", e);
            return false;
        }
    }

    public int countJobsByUserId(int userId) {
        return jobDao.countJobsByUserId(userId);
    }

    // Application methods
    public List<ApplicationEntity> getAllApplications() {
        return applicationDao.getAllApplications();
    }

    public List<ApplicationEntity> getApplicationsByUserId(int userId) {
        return applicationDao.getApplicationsByUserId(userId);
    }

    public List<ApplicationEntity> getApplicationsByJobId(int jobId) {
        return applicationDao.getApplicationsByJobId(jobId);
    }

    public List<ApplicationEntity> getApplicationsByStatus(String status) {
        return applicationDao.getApplicationsByStatus(status);
    }

    public ApplicationEntity getApplicationById(int id) {
        return applicationDao.getApplicationById(id);
    }

    public void insertApplication(ApplicationEntity application) {
        applicationDao.insert(application);
    }

    public void updateApplication(ApplicationEntity application) {
        applicationDao.update(application);
    }

    public void deleteApplication(ApplicationEntity application) {
        applicationDao.delete(application);
    }

    public int getApplicationsByJobIdAndStatus(int jobId, String status) {
        return applicationDao.getApplicationsByJobIdAndStatus(jobId, status);
    }

    public void createSampleApplications() {
        List<UserEntity> users = getUsersByRole("WORKER");
        List<JobEntity> jobs = getAllJobs();

        if (!users.isEmpty() && !jobs.isEmpty()) {
            UserEntity worker1 = users.get(0);
            UserEntity worker2 = users.get(1);
            UserEntity worker3 = users.get(2);

            // Worker 1 ứng tuyển 2 job
            insertApplication(createApplication(worker1, jobs.get(0), "Em có kinh nghiệm phục vụ bàn."));
            insertApplication(createApplication(worker1, jobs.get(1), "Em rất đam mê pha chế đồ uống."));

            // Worker 2 ứng tuyển 1 job
            insertApplication(createApplication(worker2, jobs.get(2), "Em có thể giao hàng vào cuối tuần."));

            // Worker 3 ứng tuyển 2 job
            insertApplication(createApplication(worker3, jobs.get(3), "Em từng làm đóng gói 6 tháng."));
            insertApplication(createApplication(worker3, jobs.get(4), "Em thích làm việc với khách hàng."));
        }
    }

    private ApplicationEntity createApplication(UserEntity user, JobEntity job, String message) {
        String applicationId = UUID.randomUUID().toString();
        long now = new Date().getTime();

        return new ApplicationEntity(
                0, // ID auto-generate
                applicationId,
                job.getId(),
                user.getId(),
                message,
                null, // otherFileUrl
                null, // cvFileName
                null, // cvFileUri
                "pending",
                null,
                now);

    }

    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    // Verification methods
    public void updateVerificationCode(String email, String code, Date expiresAt) {
        userDao.updateVerificationCode(email, code, expiresAt);
    }

    public boolean verifyUser(String email, String code, Date currentTime) {
        int updatedRows = userDao.verifyUser(email, code, currentTime);
        return updatedRows > 0;
    }

    // Report methods
    public List<ReportEntity> getAllReports() {
        return reportDao.getAllReports();
    }

    public void insertReport(ReportEntity report) {
        reportDao.insert(report);
    }

    public boolean updateReport(ReportEntity report) {
        try {
            reportDao.update(report);
            return true;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error updating report", e);
            return false;
        }
    }

    public boolean deleteReport(String reportId) {
        try {
            reportDao.deleteById(reportId);
            return true;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error deleting report", e);
            return false;
        }
    }

    public ReportEntity getReportById(String reportId) {
        return reportDao.getReportById(reportId);
    }

    public List<ReportEntity> getReportsByStatus(String status) {
        return reportDao.getReportsByStatus(status);
    }

    // Utility methods - converter methods
    public UserEntity convertUserToEntity(User user) {
        if (user == null)
            return null;

        UserEntity entity = new UserEntity();
        entity.setUserId(user.getUserId() != null ? user.getUserId().toString() : UUID.randomUUID().toString());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setFullName(user.getFullName());
        entity.setPhoneNumber(user.getPhoneNumber());
        // entity.setAddress(user.getAddress()); // Comment out since User doesn't have
        // getAddress()
        entity.setDescription(user.getDescription());
        entity.setRole(user.getRole());
        entity.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt() : new Date());
        // entity.setUpdatedAt(new Date()); // Comment out since UserEntity doesn't have
        // setUpdatedAt()
        entity.setIsVerified(user.isVerified()); // Use isVerified() instead of getIsVerified()
        return entity;
    }

    public User convertEntityToUser(UserEntity entity) {
        if (entity == null)
            return null;

        User user = new User();
        user.setUserId(UUID.fromString(entity.getUserId()));
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setFullName(entity.getFullName());
        user.setPhoneNumber(entity.getPhoneNumber());
        // user.setAddress(entity.getAddress()); // Comment out since User doesn't have
        // setAddress()
        user.setDescription(entity.getDescription());
        user.setRole(entity.getRole());
        user.setCreatedAt(entity.getCreatedAt());
        // user.setUpdatedAt(entity.getUpdatedAt()); // Comment out since User doesn't
        // have setUpdatedAt()
        user.setVerified(entity.getIsVerified()); // Use setVerified() instead of setIsVerified()
        return user;
    }

    // Sample data initialization
    public void initializeSampleData() {
        // Kiểm tra xem đã có data chưa
        try {
            List<UserEntity> users = userDao.getAll();
            if (users.isEmpty()) {
                Log.d(TAG, "Initializing sample data...");

                // 1. Tạo admin user
                UserEntity admin = new UserEntity();
                admin.setUserId(UUID.randomUUID().toString());
                admin.setEmail("admin@job.com");
                admin.setPassword("admin123");
                admin.setFullName("Administrator");
                admin.setPhoneNumber("0123456789");
                admin.setDescription("System Administrator");
                admin.setRole("ADMIN");
                admin.setCreatedAt(new Date());
                admin.setIsVerified(true);
                admin.setIsActive(true);
                admin.setGender(true); // Male
                userDao.insert(admin);

                // 2. Tạo employer users
                UserEntity employer1 = new UserEntity();
                employer1.setUserId(UUID.randomUUID().toString());
                employer1.setEmail("employer1@company.com");
                employer1.setPassword("123456");
                employer1.setFullName("Nguyễn Văn A");
                employer1.setPhoneNumber("0987654321");
                employer1.setDescription("Quản lý nhân sự tại Công ty ABC");
                employer1.setRole("EMPLOYER");
                employer1.setCreatedAt(new Date());
                employer1.setIsVerified(true);
                employer1.setIsActive(true);
                employer1.setGender(true);
                employer1.setPostQuota(100);
                userDao.insert(employer1);

                UserEntity employer2 = new UserEntity();
                employer2.setUserId(UUID.randomUUID().toString());
                employer2.setEmail("employer2@startup.com");
                employer2.setPassword("123456");
                employer2.setFullName("Trần Thị B");
                employer2.setPhoneNumber("0976543210");
                employer2.setDescription("Founder của Startup XYZ");
                employer2.setRole("EMPLOYER");
                employer2.setCreatedAt(new Date());
                employer2.setIsVerified(true);
                employer2.setIsActive(true);
                employer2.setGender(false);
                employer2.setPostQuota(50);
                userDao.insert(employer2);

                // 3. Tạo worker users
                UserEntity worker1 = new UserEntity();
                worker1.setUserId(UUID.randomUUID().toString());
                worker1.setEmail("worker1@gmail.com");
                worker1.setPassword("123456");
                worker1.setFullName("Lê Văn C");
                worker1.setPhoneNumber("0965432109");
                worker1.setDescription("Lập trình viên Java với 3 năm kinh nghiệm");
                worker1.setRole("WORKER");
                worker1.setCreatedAt(new Date());
                worker1.setIsVerified(true);
                worker1.setIsActive(true);
                worker1.setGender(true);
                userDao.insert(worker1);

                UserEntity worker2 = new UserEntity();
                worker2.setUserId(UUID.randomUUID().toString());
                worker2.setEmail("worker2@gmail.com");
                worker2.setPassword("123456");
                worker2.setFullName("Phạm Thị D");
                worker2.setPhoneNumber("0954321098");
                worker2.setDescription("Designer UI/UX chuyên nghiệp");
                worker2.setRole("WORKER");
                worker2.setCreatedAt(new Date());
                worker2.setIsVerified(true);
                worker2.setIsActive(true);
                worker2.setGender(false);
                userDao.insert(worker2);

                UserEntity worker3 = new UserEntity();
                worker3.setUserId(UUID.randomUUID().toString());
                worker3.setEmail("worker3@gmail.com");
                worker3.setPassword("123456");
                worker3.setFullName("Hoàng Văn E");
                worker3.setPhoneNumber("0943210987");
                worker3.setDescription("Thợ sửa chữa điện tử");
                worker3.setRole("WORKER");
                worker3.setCreatedAt(new Date());
                worker3.setIsVerified(false); // Chưa xác thực
                worker3.setIsActive(true);
                worker3.setGender(true);
                userDao.insert(worker3);

                UserEntity worker4 = new UserEntity();
                worker4.setUserId(UUID.randomUUID().toString());
                worker4.setEmail("worker4@gmail.com");
                worker4.setPassword("123456");
                worker4.setFullName("Đặng Thị F");
                worker4.setPhoneNumber("0932109876");
                worker4.setDescription("Kế toán viên");
                worker4.setRole("WORKER");
                worker4.setCreatedAt(new Date());
                worker4.setIsVerified(true);
                worker4.setIsActive(false); // Tài khoản bị khóa
                worker4.setGender(false);
                userDao.insert(worker4);

                // 4. Tạo sample jobs
                JobEntity job1 = new JobEntity();
                job1.setTitle("Tuyển Lập trình viên Java");
                job1.setDescription("Cần tuyển lập trình viên Java có kinh nghiệm 2+ năm");
                job1.setLocation("Hà Nội");
                job1.setSalary("15-25 triệu VND");
                job1.setJobType("OFFERING");
                job1.setCreatedAt(new Date());
                job1.setIsActive(true);
                jobDao.insert(job1);

                JobEntity job2 = new JobEntity();
                job2.setTitle("Cần thuê Designer");
                job2.setDescription("Thiết kế logo và banner cho startup");
                job2.setLocation("TP.HCM");
                job2.setSalary("5-10 triệu VND");
                job2.setJobType("REQUESTING");
                job2.setCreatedAt(new Date());
                job2.setIsActive(true);
                jobDao.insert(job2);

                Log.d(TAG, "Sample data initialized successfully with " + 
                    "1 admin, 2 employers, 4 workers, and 2 jobs");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing sample data", e);
        }
    }

    /**
     * Force reset and reinitialize database with fresh sample data
     * Useful for testing and development
     */
    public void resetAndInitializeSampleData() {
        try {
            Log.d(TAG, "Resetting database and initializing fresh sample data...");
            
            // Clear main tables (only the ones we have deleteAll methods for)
            userDao.deleteAll();
            jobDao.deleteAll();
            
            // For other tables, we can delete specific records if needed
            // applicationDao doesn't have deleteAll(), so skip it
            // reportDao doesn't have deleteAll(), so skip it  
            // chatDao doesn't have deleteAll(), so skip it
            
            Log.d(TAG, "Main tables cleared (users, jobs)");
            
            // Force reinitialize sample data
            initializeSampleData();
            
            Log.d(TAG, "Database reset and reinitialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting database", e);
            throw e;
        }
    }

    // Nearby jobs method
    public List<JobEntity> getNearbyJobs(int limit) {
        try {
            return jobDao.getNearbyJobs(limit); // Use existing getNearbyJobs method
        } catch (Exception e) {
            Log.e(TAG, "Error getting nearby jobs", e);
            return jobDao.getAllActiveJobs();
        }
    }

    // Verification code method
    public void clearVerificationCode(String email) {
        try {
            UserEntity user = userDao.getByEmail(email);
            if (user != null) {
                // Assuming verification code is stored in a field, clear it
                user.setIsVerified(true);
                userDao.update(user);
                Log.d(TAG, "Cleared verification code for: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing verification code", e);
        }
    }

    /**
     * Debug method để kiểm tra mật khẩu hiện tại của user
     */
    public String getCurrentPassword(String email) {
        try {
            UserEntity user = userDao.getByEmail(email);
            if (user != null) {
                android.util.Log.d("DatabaseHelper", "Current password for " + email + " is: " + user.getPassword());
                return user.getPassword();
            }
            return null;
        } catch (Exception e) {
            android.util.Log.e("DatabaseHelper", "Error getting current password", e);
            return null;
        }
    }

    // Chat methods
    public void insertChatMessage(ChatEntity message) {
        chatDao.insert(message);
    }

    public List<ChatEntity> getMessagesBetweenUsers(int userId1, int userId2, int jobId) {
        return chatDao.getMessagesBetweenUsers(userId1, userId2, jobId);
    }

    public List<ChatEntity> getConversations(int userId) {
        return chatDao.getConversations(userId);
    }

    public List<ChatEntity> getConversationsByJob(int userId, int jobId) {
        return chatDao.getConversationsByJob(userId, jobId);
    }

    public List<ChatEntity> getUnreadMessages(int userId) {
        return chatDao.getUnreadMessages(userId);
    }

    public void markMessagesAsRead(int userId, int senderId, int jobId) {
        chatDao.markMessagesAsRead(userId, senderId, jobId);
    }

    public ChatEntity getLatestMessage(int userId1, int userId2, int jobId) {
        return chatDao.getLatestMessage(userId1, userId2, jobId);
    }

    public int getUnreadMessageCount(int userId) {
        return chatDao.getUnreadMessageCount(userId);
    }
}
