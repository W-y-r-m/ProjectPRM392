package com.example.projectprm392.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectprm392.R;
import com.example.projectprm392.adapters.ApprovedUsersAdapter;

import java.util.List;

public class ApprovedUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ApprovedUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approved_users);
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        // Hiển thị nút back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách người được duyệt");
        }
        recyclerView = findViewById(R.id.recyclerViewApprovedUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 1. Lấy jobId từ Intent
        int jobId = getIntent().getIntExtra("jobId", -1);

        // 2. Lấy danh sách user đã được approve cho jobId
        com.example.projectprm392.database.DatabaseHelper db = new com.example.projectprm392.database.DatabaseHelper(
                this);
        java.util.List<com.example.projectprm392.database.ApplicationEntity> applications = db
                .getApplicationsByJobId(jobId);
        java.util.List<com.example.projectprm392.database.UserEntity> approvedUsers = new java.util.ArrayList<>();
        for (com.example.projectprm392.database.ApplicationEntity app : applications) {
            if ("Approved".equalsIgnoreCase(app.getStatus())) {
                com.example.projectprm392.database.UserEntity user = db.getUserById(app.getUserId());
                if (user != null)
                    approvedUsers.add(user);
            }
        }

        // 3. Hiển thị lên RecyclerView
        adapter = new com.example.projectprm392.adapters.ApprovedUsersAdapter(approvedUsers, String.valueOf(jobId));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
