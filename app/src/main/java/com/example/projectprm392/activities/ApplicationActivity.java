package com.example.projectprm392.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.ApplicationAdapter;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.utils.SessionManager;

import java.util.List;

public class ApplicationActivity extends AppCompatActivity {

    private RecyclerView recyclerApplications;
    private ApplicationAdapter adapter;
    private DatabaseHelper databaseHelper;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_application);

        // Khởi tạo helper
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.initializeSampleData();

        // Khởi tạo nút Back
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Gán RecyclerView
        recyclerApplications = findViewById(R.id.recyclerApplications);
        recyclerApplications.setLayoutManager(new LinearLayoutManager(this));

        // Load dữ liệu
        loadApplications();
    }

    private void loadApplications() {
        // Lấy userId từ session
        SessionManager sessionManager = new SessionManager(this);
        String userIdStr = sessionManager.getUserId();
        List<ApplicationEntity> applications;
        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                // Lấy tất cả job mà user này là chủ sở hữu
                List<JobEntity> jobs = databaseHelper.getJobsByUserId(userId);
                applications = new java.util.ArrayList<>();
                for (JobEntity job : jobs) {
                    List<ApplicationEntity> apps = databaseHelper.getApplicationsByJobId(job.getId());
                    if (apps != null && !apps.isEmpty()) {
                        applications.addAll(apps);
                    }
                }
            } catch (Exception e) {
                applications = databaseHelper.getAllApplications();
            }
        } else {
            applications = databaseHelper.getAllApplications();
        }
        adapter = new ApplicationAdapter(databaseHelper, applications);
        recyclerApplications.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}