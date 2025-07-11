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
    private JobDao jobDao;
    private ApplicationDao applicationDao;
    private ExecutorService executor;

    public DatabaseHelper(Context context) {
        database = AppDatabase.getDatabase(context);
        userDao = database.userDao();
        jobDao = database.jobDao();
        applicationDao = database.applicationDao();
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
        return userDao.getByEmailAndPassword(email, password);
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
            int rowsAffected = userDao.updatePassword(email, hashedPassword);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clear verification code for user by email
     */
    public void clearVerificationCode(String email) {
        try {
            userDao.clearVerificationCode(email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Convert methods
    public UserEntity convertUserToEntity(User user) {
        if (user == null)
            return null;
        try {
            UserEntity entity = new UserEntity();
            entity.setUserId(user.getUserId() != null ? user.getUserId().toString() : UUID.randomUUID().toString());
            entity.setEmail(user.getEmail());
            entity.setPhoneNumber(user.getPhoneNumber());
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
            entity.setIsVerified(user.isVerified());
            entity.setVerificationCode(user.getVerificationCode());
            entity.setVerificationCodeExpiresAt(user.getVerificationCodeExpiresAt());

            return entity;
        } catch (Exception e) {
            System.err.println("Error converting user to entity: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public User convertEntityToUser(UserEntity entity) {
        if (entity == null)
            return null;
        try {
            User user = new User();
            user.setUserId(UUID.fromString(entity.getUserId()));
            user.setEmail(entity.getEmail());
            user.setPassword(entity.getPassword());
            user.setFullName(entity.getFullName());
            user.setGender(entity.getGender());
            user.setRole(entity.getRole());
            user.setPhoneNumber(entity.getPhoneNumber());
            user.setLevelOfViolation(entity.getLevelOfViolation());
            user.setDescription(entity.getDescription());
            user.setPostQuota(entity.getPostQuota());
            user.setCurrentLatitude(entity.getCurrentLatitude());
            user.setCurrentLongitude(entity.getCurrentLongitude());
            user.setCreatedAt(entity.getCreatedAt());
            user.setIsActive(entity.getIsActive());
            user.setVerified(entity.getIsVerified());
            user.setVerificationCode(entity.getVerificationCode());
            user.setVerificationCodeExpiresAt(entity.getVerificationCodeExpiresAt());

            return user;
        } catch (Exception e) {
            System.err.println("Error converting entity to user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Initialize với dữ liệu mẫu
    public void initializeSampleData() {
        // Kiểm tra xem có data chưa
        if (userDao.getAll().isEmpty()) {
            // Create test accounts with verified status
            User testWorker = new User("test@gmail.com", "123456", "Nguyen Van A", "worker");
            testWorker.setGender(true);
            testWorker.setDescription("Test worker account");
            testWorker.setCurrentLatitude(21.0285);
            testWorker.setCurrentLongitude(105.8542);
            testWorker.setCreatedAt(new Date());
            testWorker.setVerified(true); // Pre-verified for testing

            User testEmployer = new User("employer@gmail.com", "123456", "Tran Thi B", "employer");
            testEmployer.setGender(false);
            testEmployer.setDescription("Test employer account");
            testEmployer.setCurrentLatitude(21.0195);
            testEmployer.setCurrentLongitude(105.8325);
            testEmployer.setPostQuota(100);
            testEmployer.setCreatedAt(new Date());
            testEmployer.setVerified(true); // Pre-verified for testing

            // Insert test accounts
            insertUser(testWorker);
            insertUser(testEmployer);

            // Tạo user mẫu khác
            User worker1 = new User("nguyenvana@gmail.com", "123456", "Nguyễn Văn A", "WORKER");
            worker1.setPhoneNumber("0909123456");
            worker1.setGender(true);
            worker1.setDescription("Tôi có kinh nghiệm làm việc phục vụ bàn, giao hàng và bán hàng");
            worker1.setCurrentLatitude(21.0285);
            worker1.setCurrentLongitude(105.8542);
            worker1.setCreatedAt(new Date());

            User worker2 = new User("tranthib@gmail.com", "123456", "Trần Thị B", "WORKER");
            worker2.setPhoneNumber("0909234567");
            worker2.setGender(false);
            worker2.setDescription("Sinh viên năm 3, tìm việc làm thêm cuối tuần");
            worker2.setCurrentLatitude(21.0245);
            worker2.setCurrentLongitude(105.8412);
            worker2.setCreatedAt(new Date());

            User worker3 = new User("lehoanc@gmail.com", "123456", "Lê Hoàn C", "WORKER");
            worker3.setPhoneNumber("0909345678");
            worker3.setGender(true);
            worker3.setDescription("Có bằng lái xe máy, kinh nghiệm giao hàng 2 năm");
            worker3.setCurrentLatitude(21.0325);
            worker3.setCurrentLongitude(105.8485);
            worker3.setCreatedAt(new Date());

            User employer1 = new User("cafehanoi@gmail.com", "123456", "Cafe Hà Nội", "EMPLOYER");
            employer1.setPhoneNumber("0909456789");
            employer1.setGender(null);
            employer1.setDescription("Chuỗi cafe cần tuyển nhân viên phục vụ");
            employer1.setCurrentLatitude(21.0285);
            employer1.setCurrentLongitude(105.8542);
            employer1.setPostQuota(50);
            employer1.setCreatedAt(new Date());

            User employer2 = new User("shoponline@gmail.com", "123456", "Shop Online ABC", "EMPLOYER");
            employer2.setPhoneNumber("0909567890");
            employer2.setGender(null);
            employer2.setDescription("Shop thời trang online cần shipper giao hàng");
            employer2.setCurrentLatitude(21.0195);
            employer2.setCurrentLongitude(105.8325);
            employer2.setPostQuota(30);
            employer2.setCreatedAt(new Date());

            User employer3 = new User("nhahangtuan@gmail.com", "123456", "Nhà Hàng Tuấn", "EMPLOYER");
            employer3.setPhoneNumber("0909678901");
            employer3.setGender(null);
            employer3.setDescription("Nhà hàng gia đình cần nhân viên bán thời gian");
            employer3.setCurrentLatitude(21.0355);
            employer3.setCurrentLongitude(105.8445);
            employer3.setPostQuota(20);
            employer3.setCreatedAt(new Date());

            insertUser(worker1);
            insertUser(worker2);
            insertUser(worker3);
            insertUser(employer1);
            insertUser(employer2);
            insertUser(employer3);

            // Tạo dữ liệu job mẫu
            createSampleJobs();
        }
    }

    // Job helper methods
    public void createSampleJobs() {
        // Lấy danh sách employers để tạo jobs
        List<UserEntity> employers = getUsersByRole("EMPLOYER");

        if (!employers.isEmpty()) {
            UserEntity cafe = employers.get(0);
            UserEntity shop = employers.get(1);
            UserEntity restaurant = employers.get(2);

            // Jobs từ Cafe Hà Nội
            createJobForUser(cafe, "Nhân viên phục vụ bàn",
                    "Cần tuyển nhân viên phục vụ bàn cho quán cafe. Yêu cầu giao tiếp tốt, thân thiện với khách hàng. Làm việc trong môi trường trẻ trung, năng động.",
                    "180,000đ/ca", "Hà Nội", "PART_TIME", "ENTRY", 2, "8:00 - 17:00",
                    21.0285, 105.8542);

            createJobForUser(cafe, "Pha chế cafe",
                    "Tuyển barista có kinh nghiệm pha chế. Biết làm các loại đồ uống cơ bản. Đào tạo thêm các kỹ năng chuyên sâu.",
                    "200,000đ/ca", "Hà Nội", "PART_TIME", "JUNIOR", 1, "6:00 - 14:00",
                    21.0285, 105.8542);

            // Jobs từ Shop Online
            createJobForUser(shop, "Shipper giao hàng",
                    "Cần shipper giao hàng online. Có xe máy, biết đường Hà Nội. Làm việc linh hoạt theo ca.",
                    "25,000đ/đơn", "Hà Nội", "FREELANCE", "ENTRY", 5, "9:00 - 18:00",
                    21.0195, 105.8325);

            createJobForUser(shop, "Đóng gói hàng",
                    "Tuyển nhân viên đóng gói sản phẩm. Làm việc tại kho. Công việc nhẹ nhàng phù hợp nữ giới.",
                    "150,000đ/ca", "Hà Nội", "PART_TIME", "ENTRY", 3, "14:00 - 22:00",
                    21.0195, 105.8325);

            // Jobs từ Nhà Hàng
            createJobForUser(restaurant, "Phục vụ bàn nhà hàng",
                    "Cần nhân viên phục vụ cho nhà hàng gia đình. Làm ca tối, cuối tuần. Môi trường làm việc thân thiện.",
                    "170,000đ/ca", "Hà Nội", "PART_TIME", "ENTRY", 2, "17:00 - 22:00",
                    21.0355, 105.8445);

            createJobForUser(restaurant, "Bếp phụ",
                    "Tuyển bếp phụ chuẩn bị nguyên liệu, rửa bát. Không cần kinh nghiệm. Được đào tạo từ đầu.",
                    "160,000đ/ca", "Hà Nội", "PART_TIME", "ENTRY", 1, "16:00 - 23:00",
                    21.0355, 105.8445);

            // Thêm nhiều việc làm đa dạng khác
            createJobForUser(cafe, "Cashier - Thu ngân",
                    "Tuyển thu ngân cho cửa hàng. Biết sử dụng máy tính cơ bản, giao tiếp tốt với khách hàng.",
                    "4,500,000đ/tháng", "Hà Nội", "FULL_TIME", "ENTRY", 1, "8:00 - 17:00",
                    21.0300, 105.8500);

            createJobForUser(shop, "Content Creator",
                    "Tuyển content creator cho shop online. Biết chụp ảnh, viết caption, quản lý fanpage.",
                    "350,000đ/ngày", "Hà Nội", "FREELANCE", "JUNIOR", 2, "Linh hoạt",
                    21.0200, 105.8400);

            createJobForUser(restaurant, "Bảo vệ",
                    "Cần bảo vệ làm ca đêm cho nhà hàng. Có chứng chỉ bảo vệ ưu tiên.",
                    "200,000đ/đêm", "Hà Nội", "PART_TIME", "ENTRY", 1, "22:00 - 6:00",
                    21.0380, 105.8460);

            // Thêm việc làm từ các employer khác
            UserEntity employer4 = createNewEmployer("supermarketabc@gmail.com", "Siêu Thị ABC",
                    "Chuỗi siêu thị cần tuyển nhiều vị trí", 21.0250, 105.8380);

            createJobForUser(employer4, "Nhân viên bán hàng",
                    "Tuyển nhân viên bán hàng cho siêu thị. Ca sáng, chiều, tối. Được đào tạo kỹ năng bán hàng.",
                    "5,000,000đ/tháng", "Hà Nội", "FULL_TIME", "ENTRY", 5, "Theo ca",
                    21.0250, 105.8380);

            createJobForUser(employer4, "Nhân viên kho",
                    "Cần nhân viên sắp xếp hàng hóa trong kho. Làm việc ban ngày, không làm chủ nhật.",
                    "180,000đ/ca", "Hà Nội", "PART_TIME", "ENTRY", 3, "8:00 - 16:00",
                    21.0250, 105.8380);

            UserEntity employer5 = createNewEmployer("giaseupizza@gmail.com", "Pizza Gia Seu",
                    "Chuỗi pizza cần tuyển nhân viên", 21.0400, 105.8300);

            createJobForUser(employer5, "Nhân viên giao pizza",
                    "Tuyển shipper giao pizza. Có xe máy, mũ bảo hiểm. Lương cao, tips từ khách.",
                    "30,000đ/đơn", "Hà Nội", "PART_TIME", "ENTRY", 4, "17:00 - 23:00",
                    21.0400, 105.8300);

            createJobForUser(employer5, "Thợ làm pizza",
                    "Cần thợ làm pizza có kinh nghiệm. Biết làm bánh, pha chế nước uống.",
                    "250,000đ/ca", "Hà Nội", "PART_TIME", "EXPERIENCED", 2, "11:00 - 22:00",
                    21.0400, 105.8300);
        }
    }

    private UserEntity createNewEmployer(String email, String companyName, String description,
            double latitude, double longitude) {
        User newEmployer = new User(email, "123456", companyName, "EMPLOYER");
        newEmployer.setGender(null);
        newEmployer.setDescription(description);
        newEmployer.setCurrentLatitude(latitude);
        newEmployer.setCurrentLongitude(longitude);
        newEmployer.setPostQuota(25);
        newEmployer.setCreatedAt(new Date());

        long id = insertUser(newEmployer);
        return userDao.getByEmail(email);
    }

    private void createJobForUser(UserEntity employer, String title, String description,
            String salary, String location, String jobType,
            String experienceLevel, int neededAmount, String workingTime,
            double latitude, double longitude) {
        JobEntity job = new JobEntity();
        job.setJobId(UUID.randomUUID().toString());
        job.setUserId(employer.getId());
        job.setTitle(title);
        job.setDescription(description);
        job.setSalary(salary);
        job.setLocation(location);
        job.setJobType(jobType);
        job.setExperienceLevel(experienceLevel);
        job.setNeededAmount(neededAmount);
        job.setWorkingTime(workingTime);
        job.setLocationLatitude(latitude);
        job.setLocationLongitude(longitude);
        job.setPostType("JOB_POSTING"); // Set default post type for existing jobs
        job.setStatus("ACTIVE");
        job.setCreatedAt(new Date());
        job.setIsActive(true);

        jobDao.insert(job);
    }

    // Job methods
    public List<JobEntity> getAllJobs() {
        return jobDao.getAllActiveJobs();
    }

    public List<JobEntity> getNearbyJobs(int limit) {
        return jobDao.getNearbyJobs(limit);
    }

    public List<JobEntity> getJobsByType(String jobType) {
        return jobDao.getJobsByType(jobType);
    }

    public List<JobEntity> getJobsByExperienceLevel(String experienceLevel) {
        return jobDao.getJobsByExperienceLevel(experienceLevel);
    }

    public List<JobEntity> searchJobs(String keyword) {
        return jobDao.searchJobs(keyword);
    }

    public List<JobEntity> getJobsByUserId(int userId) {
        return jobDao.getJobsByUserId(userId);
    }

    public JobEntity getJobById(int id) {
        return jobDao.getById(id);
    }

    public long insertJob(JobEntity job) {
        return jobDao.insert(job);
    }

    public void updateJob(JobEntity job) {
        jobDao.update(job);
    }

    public void deleteJob(JobEntity job) {
        jobDao.delete(job);
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
                null,
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
}
