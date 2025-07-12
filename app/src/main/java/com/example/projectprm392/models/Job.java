package com.example.projectprm392.models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

public class Job {
    private UUID jobId;
    private UUID userId;
    private String title;
    private String description;
    private BigDecimal salary;
    private SalaryUnit salaryUnit;
    private Time startTime;
    private Time endTime;
    private Date workingDate;
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer neededAmount;
    private JobStatus status;
    private Timestamp createdAt;
    private Boolean isDeleted;
    
    // Thêm trường để phân biệt job posting và job seeking
    private JobType jobType;
    private JobCategory category;
    private JobLevel level;
    private String locationAddress; // Địa chỉ text để hiển thị

    public enum SalaryUnit {
        HOUR("hour"),
        DAY("day"),
        PACKAGE("package");

        private final String value;

        SalaryUnit(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobStatus {
        OPEN("open"),
        CLOSED("closed"),
        IN_PROGRESS("in_progress");

        private final String value;

        JobStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobType {
        JOB_POSTING("job_posting"),  // Đăng tin tuyển dụng
        JOB_SEEKING("job_seeking");  // Đăng tin tìm việc

        private final String value;

        JobType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobCategory {
        TECHNOLOGY("technology"),
        MARKETING("marketing"),
        SALES("sales"),
        FINANCE("finance"),
        HUMAN_RESOURCES("human_resources"),
        OPERATIONS("operations"),
        CUSTOMER_SERVICE("customer_service"),
        DESIGN("design"),
        EDUCATION("education"),
        HEALTHCARE("healthcare"),
        CONSTRUCTION("construction"),
        HOSPITALITY("hospitality"),
        TRANSPORTATION("transportation"),
        RETAIL("retail"),
        MANUFACTURING("manufacturing"),
        AGRICULTURE("agriculture"),
        MEDIA("media"),
        LEGAL("legal"),
        CONSULTING("consulting"),
        OTHER("other");

        private final String value;

        JobCategory(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum JobLevel {
        ENTRY_LEVEL("entry_level"),
        JUNIOR("junior"),
        MID_LEVEL("mid_level"),
        SENIOR("senior"),
        LEAD("lead"),
        MANAGER("manager"),
        DIRECTOR("director"),
        EXECUTIVE("executive"),
        INTERNSHIP("internship"),
        FREELANCE("freelance");

        private final String value;

        JobLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Constructors
    public Job() {
        this.jobId = UUID.randomUUID();
        this.status = JobStatus.OPEN;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.isDeleted = false;
    }

    public Job(UUID userId, String title, String description, BigDecimal salary, SalaryUnit salaryUnit) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.salaryUnit = salaryUnit;
    }

    // Constructor cho job seeking (tìm việc)
    public Job(UUID userId, String title, String description, Integer neededAmount, JobType jobType) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.neededAmount = neededAmount != null ? neededAmount : 1; // Mặc định là 1 nếu null
        this.jobType = jobType;
    }

    // Constructor cho job posting (tuyển dụng)
    public Job(UUID userId, String title, String description, BigDecimal salary, SalaryUnit salaryUnit, 
              JobCategory category, JobLevel level, Time startTime, Time endTime, Date workingDate, 
              Double locationLatitude, Double locationLongitude, String locationAddress) {
        this();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.salaryUnit = salaryUnit;
        this.category = category;
        this.level = level;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workingDate = workingDate;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationAddress = locationAddress;
        this.jobType = JobType.JOB_POSTING;
    }

    // Getters and Setters
    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public SalaryUnit getSalaryUnit() {
        return salaryUnit;
    }

    public void setSalaryUnit(SalaryUnit salaryUnit) {
        this.salaryUnit = salaryUnit;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Date getWorkingDate() {
        return workingDate;
    }

    public void setWorkingDate(Date workingDate) {
        this.workingDate = workingDate;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public Integer getNeededAmount() {
        return neededAmount;
    }

    public void setNeededAmount(Integer neededAmount) {
        this.neededAmount = neededAmount;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // Getters and Setters cho các trường mới
    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public JobCategory getCategory() {
        return category;
    }

    public void setCategory(JobCategory category) {
        this.category = category;
    }

    public JobLevel getLevel() {
        return level;
    }

    public void setLevel(JobLevel level) {
        this.level = level;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    // Utility methods
    public boolean isJobSeeking() {
        return JobType.JOB_SEEKING.equals(this.jobType);
    }

    public boolean isJobPosting() {
        return JobType.JOB_POSTING.equals(this.jobType);
    }

    public String getFormattedSalary() {
        if (salary == null) return "";
        return salary.toString() + " VND/" + (salaryUnit != null ? salaryUnit.getValue() : "");
    }

    public String getFormattedWorkingTime() {
        if (startTime == null || endTime == null) return "";
        return startTime.toString() + " - " + endTime.toString();
    }
}
