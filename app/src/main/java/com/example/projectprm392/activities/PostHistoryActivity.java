
package com.example.projectprm392.activities;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.projectprm392.fragments.HeaderFragment;
import com.example.projectprm392.fragments.FooterFragment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectprm392.R;
import com.example.projectprm392.adapters.PostHistoryAdapter;
import com.example.projectprm392.database.AppDatabase;
import com.example.projectprm392.database.JobDao;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserDao;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class PostHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostHistoryAdapter adapter;
    private List<JobEntity> jobList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_history);

        // Gắn header và footer fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentHeader, new HeaderFragment());
        fragmentTransaction.replace(R.id.fragmentFooter, new FooterFragment());
        fragmentTransaction.commit();

        recyclerView = findViewById(R.id.recyclerViewPostHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Nút quay về Home
        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(PostHistoryActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Lấy userId (String) từ SessionManager
        SessionManager sessionManager = new SessionManager(this);
        String userIdStr = sessionManager.getUserId();
        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();
        UserEntity user = userDao.getByUserId(userIdStr);
        int userIdInt = user != null ? user.getId() : -1;
        JobDao jobDao = db.jobDao();
        jobList = new ArrayList<>();
        if (userIdInt != -1) {
            jobList = jobDao.getJobsByUserId(userIdInt);
        }
        if (jobList == null || jobList.isEmpty()) {
            Toast.makeText(this, "Bạn chưa có bài đăng job nào!", Toast.LENGTH_SHORT).show();
        }
        adapter = new PostHistoryAdapter(jobList);
        recyclerView.setAdapter(adapter);
        // Nút tạo report
        Button btnCreateReport = findViewById(R.id.btnCreateReport);
        btnCreateReport.setOnClickListener(v -> {
            Intent intent = new Intent(PostHistoryActivity.this, ReportActivity.class);
            startActivity(intent);
        });
    }
}
