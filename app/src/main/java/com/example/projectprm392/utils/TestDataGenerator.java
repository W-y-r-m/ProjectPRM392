package com.example.projectprm392.utils;

import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.ReportEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.JobEntity;
import java.util.List;

public class TestDataGenerator {
    
    private DatabaseHelper databaseHelper;
    
    public TestDataGenerator(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void generateTestReports() {
        // Get existing users and jobs
        List<UserEntity> users = databaseHelper.getAllUsers();
        List<JobEntity> jobs = databaseHelper.getAllJobs();
        
        if (users.size() < 2) {
            // Create some test users if not enough
            createTestUsers();
            users = databaseHelper.getAllUsers();
        }
        
        if (jobs.size() < 1) {
            // Create some test jobs if none exist
            createTestJobs(users);
            jobs = databaseHelper.getAllJobs();
        }
        
        // Create test reports
        createTestReports(users, jobs);
    }
    
    private void createTestUsers() {
        // Create test user 1
        UserEntity user1 = new UserEntity();
        user1.setUserId("test_user_1");
        user1.setFullName("Nguyễn Văn A");
        user1.setEmail("nguyenvana@test.com");
        user1.setPassword("123456");
        user1.setPhoneNumber("0901234567");
        user1.setRole("user");
        user1.setIsVerified(true);
        databaseHelper.insertUser(user1);
        
        // Create test user 2
        UserEntity user2 = new UserEntity();
        user2.setUserId("test_user_2");
        user2.setFullName("Trần Thị B");
        user2.setEmail("tranthib@test.com");
        user2.setPassword("123456");
        user2.setPhoneNumber("0901234568");
        user2.setRole("user");
        user2.setIsVerified(true);
        databaseHelper.insertUser(user2);
        
        // Create test user 3
        UserEntity user3 = new UserEntity();
        user3.setUserId("test_user_3");
        user3.setFullName("Lê Văn C");
        user3.setEmail("levanc@test.com");
        user3.setPassword("123456");
        user3.setPhoneNumber("0901234569");
        user3.setRole("user");
        user3.setIsVerified(true);
        databaseHelper.insertUser(user3);
    }
    
    private void createTestJobs(List<UserEntity> users) {
        if (users.size() < 1) return;
        
        UserEntity user = users.get(0);
        
        // Create test job 1
        JobEntity job1 = new JobEntity();
        job1.setJobId("test_job_1");
        job1.setUserId(user.getUserId());
        job1.setTitle("Tuyển Developer Java");
        job1.setDescription("Cần tuyển developer Java có kinh nghiệm 2+ năm");
        job1.setSalary("15000000 VND");
        job1.setLocation("Hà Nội");
        job1.setPostType("JOB_POSTING");
        job1.setExperienceLevel("2+ năm");
        job1.setNeededAmount(2);
        job1.setWorkingTime("8h/ngày");
        job1.setIsActive(true);
        job1.setCreatedAt(new java.util.Date());
        databaseHelper.insertJob(job1);
        
        // Create test job 2
        JobEntity job2 = new JobEntity();
        job2.setJobId("test_job_2");
        job2.setUserId(user.getUserId());
        job2.setTitle("Tìm việc Designer");
        job2.setDescription("Tôi là designer có 3 năm kinh nghiệm, tìm việc remote");
        job2.setSalary("12000000 VND");
        job2.setLocation("TP.HCM");
        job2.setPostType("JOB_SEEKING");
        job2.setExperienceLevel("3 năm");
        job2.setNeededAmount(1);
        job2.setWorkingTime("Linh hoạt");
        job2.setIsActive(true);
        job2.setCreatedAt(new java.util.Date());
        databaseHelper.insertJob(job2);
    }
    
    private void createTestReports(List<UserEntity> users, List<JobEntity> jobs) {
        if (users.size() < 2) return;
        
        // Report 1: User 1 reports User 2 about inappropriate job posting
        ReportEntity report1 = new ReportEntity(
            users.get(0).getUserId(), // reporter
            users.get(1).getUserId(), // target user
            jobs.size() > 0 ? jobs.get(0).getJobId() : "", // job id
            "Bài đăng này có nội dung không phù hợp và có dấu hiệu lừa đảo. Yêu cầu mức lương quá cao so với công việc mô tả."
        );
        databaseHelper.insertReport(report1);
        
        // Report 2: User 2 reports User 1 for harassment
        ReportEntity report2 = new ReportEntity(
            users.get(1).getUserId(), // reporter
            users.get(0).getUserId(), // target user
            "", // no specific job
            "Người dùng này đã gửi tin nhắn quấy rối và có hành vi không phù hợp trong quá trình liên lệ về công việc."
        );
        databaseHelper.insertReport(report2);
        
        // Report 3: User 3 reports about fake job posting
        if (users.size() > 2) {
            ReportEntity report3 = new ReportEntity(
                users.get(2).getUserId(), // reporter
                users.get(1).getUserId(), // target user
                jobs.size() > 1 ? jobs.get(1).getJobId() : "", // job id
                "Bài đăng tuyển dụng này có dấu hiệu giả mạo. Thông tin công ty không chính xác và yêu cầu nộp phí."
            );
            databaseHelper.insertReport(report3);
        }
        
        // Report 4: Already resolved report
        ReportEntity report4 = new ReportEntity(
            users.get(0).getUserId(), // reporter
            users.get(1).getUserId(), // target user
            "", // no specific job
            "Báo cáo về vi phạm quy định đăng bài. Đã được xử lý trước đó."
        );
        report4.status = "resolved"; // Set as resolved
        databaseHelper.insertReport(report4);
        
        // Report 5: Rejected report
        ReportEntity report5 = new ReportEntity(
            users.get(1).getUserId(), // reporter
            users.get(0).getUserId(), // target user
            "", // no specific job
            "Báo cáo không có căn cứ rõ ràng. Chỉ là hiểu lầm trong giao tiếp."
        );
        report5.status = "rejected"; // Set as rejected
        databaseHelper.insertReport(report5);
    }
}
