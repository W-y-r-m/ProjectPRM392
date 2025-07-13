
package com.example.projectprm392.activities;
import android.widget.Toast;

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
import com.example.projectprm392.database.PostHistoryDao;
import com.example.projectprm392.database.PostHistoryEntity;
import java.util.ArrayList;
import java.util.List;

public class PostHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostHistoryAdapter adapter;
    private List<PostHistoryEntity> postHistoryList;

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

        // Lấy dữ liệu từ RoomDB
        AppDatabase db = AppDatabase.getDatabase(this);
        PostHistoryDao dao = db.postHistoryDao();
        postHistoryList = dao.getAllPostHistory();

        // Nếu chưa có dữ liệu thì thêm dữ liệu mẫu
        if (postHistoryList == null || postHistoryList.isEmpty()) {
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 1", "Nội dung bài đăng mẫu 1", "2025-07-09"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 2", "Nội dung bài đăng mẫu 2", "2025-07-08"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 3", "Nội dung bài đăng mẫu 3", "2025-07-07"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 4", "Nội dung bài đăng mẫu 4", "2025-07-06"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 5", "Nội dung bài đăng mẫu 5", "2025-07-05"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 6", "Nội dung bài đăng mẫu 6", "2025-07-04"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 7", "Nội dung bài đăng mẫu 7", "2025-07-03"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 8", "Nội dung bài đăng mẫu 8", "2025-07-02"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 9", "Nội dung bài đăng mẫu 9", "2025-07-01"));
            dao.insert(new PostHistoryEntity("Bài đăng mẫu 10", "Nội dung bài đăng mẫu 10", "2025-06-30"));
            postHistoryList = dao.getAllPostHistory();
            Toast.makeText(this, "Đã thêm nhiều dữ liệu mẫu!", Toast.LENGTH_SHORT).show();
        }

        adapter = new PostHistoryAdapter(postHistoryList);
        recyclerView.setAdapter(adapter);
    }
}
