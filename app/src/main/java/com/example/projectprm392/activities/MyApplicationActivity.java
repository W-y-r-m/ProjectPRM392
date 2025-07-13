package com.example.projectprm392.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.MyApplicationAdapter;
import com.example.projectprm392.database.AppDatabase;
import com.example.projectprm392.database.ApplicationEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserDao;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;

import java.util.List;

public class MyApplicationActivity extends AppCompatActivity {
    private RecyclerView recyclerMyApplications;
    private MyApplicationAdapter adapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_application);

        databaseHelper = new DatabaseHelper(this);

        recyclerMyApplications = findViewById(R.id.recyclerMyApplications);
        recyclerMyApplications.setLayoutManager(new LinearLayoutManager(this));

        loadMyApplications();

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void loadMyApplications() {
        SessionManager sessionManager = new SessionManager(this);
        String userIdStr = sessionManager.getUserId();
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();
        UserEntity user = userDao.getByUserId(userIdStr);
        if (user == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng!", Toast.LENGTH_LONG).show();
            recyclerMyApplications.setAdapter(null);
            return;
        }
        int userIdInt = user.getId();
        List<ApplicationEntity> myApplications = databaseHelper.getApplicationsByUserId(userIdInt);
        if (myApplications == null || myApplications.isEmpty()) {
            Toast.makeText(this, "Bạn chưa có đơn đăng ký nào!", Toast.LENGTH_LONG).show();
        }
        adapter = new MyApplicationAdapter(databaseHelper,
                myApplications != null ? myApplications : new java.util.ArrayList<>());
        recyclerMyApplications.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
