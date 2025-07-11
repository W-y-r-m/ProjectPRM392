package com.example.projectprm392.activities;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.ApplicationAdapter;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;

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
        databaseHelper.createSampleApplications();

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
        List<ApplicationEntity> applications = databaseHelper.getAllApplications();
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
