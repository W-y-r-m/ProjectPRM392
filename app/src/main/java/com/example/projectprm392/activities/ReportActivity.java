
package com.example.projectprm392.activities;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectprm392.R;
import com.example.projectprm392.database.AppDatabase;
import com.example.projectprm392.database.ReportDao;
import com.example.projectprm392.database.ReportEntity;
import com.example.projectprm392.database.UserDao;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.JobDao;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.utils.SessionManager;

public class ReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Spinner spinnerUser = findViewById(R.id.spinnerUser);
        Spinner spinnerJob = findViewById(R.id.spinnerJob);
        EditText etContent = findViewById(R.id.etContent);
        Button btnSendReport = findViewById(R.id.btnSendReport);
        Button btnBackReport = findViewById(R.id.btnBackReport);

        btnBackReport.setOnClickListener(v -> finish());

        AppDatabase db = AppDatabase.getDatabase(this);
        UserDao userDao = db.userDao();
        JobDao jobDao = db.jobDao();
        ReportDao reportDao = db.reportDao();

        // Lấy user hiện tại
        SessionManager sessionManager = new SessionManager(this);
        String currentUserId = sessionManager.getUserId();
        UserEntity currentUser = userDao.getByUserId(currentUserId);

        // Lấy danh sách user khác
        java.util.List<UserEntity> allUsers = userDao.getAll();
        java.util.List<UserEntity> otherUsers = new java.util.ArrayList<>();
        for (UserEntity u : allUsers) {
            if (currentUser == null || u.getId() != currentUser.getId()) {
                otherUsers.add(u);
            }
        }
        ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        userAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (UserEntity u : otherUsers) {
            userAdapter.add(u.getFullName() + " (" + u.getEmail() + ")");
        }
        spinnerUser.setAdapter(userAdapter);

        // Khi chọn user, load job của user đó
        spinnerUser.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                UserEntity selectedUser = otherUsers.get(position);
                java.util.List<JobEntity> jobs = jobDao.getJobsByUserId(selectedUser.getId());
                ArrayAdapter<String> jobAdapter = new ArrayAdapter<>(ReportActivity.this, android.R.layout.simple_spinner_item);
                jobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                jobAdapter.add("Không chọn job cụ thể");
                for (JobEntity job : jobs) {
                    jobAdapter.add(job.getTitle() + " (ID: " + job.getJobId() + ")");
                }
                spinnerJob.setAdapter(jobAdapter);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnSendReport.setOnClickListener(v -> {
            int userPos = spinnerUser.getSelectedItemPosition();
            if (userPos < 0 || userPos >= otherUsers.size()) {
                Toast.makeText(this, "Vui lòng chọn người dùng để report!", Toast.LENGTH_SHORT).show();
                return;
            }
            UserEntity selectedUser = otherUsers.get(userPos);
            int jobPos = spinnerJob.getSelectedItemPosition();
            String jobId = "";
            if (jobPos > 0) { // 0 là "Không chọn job cụ thể"
                java.util.List<JobEntity> jobs = jobDao.getJobsByUserId(selectedUser.getId());
                if (jobPos - 1 < jobs.size()) {
                    jobId = jobs.get(jobPos - 1).getJobId();
                }
            }
            String content = etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập nội dung báo cáo!", Toast.LENGTH_SHORT).show();
                return;
            }
            String reporterId = currentUser != null ? currentUser.getUserId() : "";
            String targetUserId = selectedUser.getUserId();
            ReportEntity report = new ReportEntity(reporterId, targetUserId, jobId, content);
            reportDao.insert(report);
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Gửi báo cáo thành công")
                .setMessage("Báo cáo của bạn đã được gửi. Cảm ơn bạn đã giúp xây dựng cộng đồng an toàn hơn!")
                .setPositiveButton("Đóng", (d, which) -> {
                    d.dismiss();
                    etContent.setText("");
                })
                .setCancelable(false)
                .create();
            dialog.show();
        });
    }
}
