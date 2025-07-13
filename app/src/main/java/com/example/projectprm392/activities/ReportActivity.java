
package com.example.projectprm392.activities;
import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.projectprm392.R;
import com.example.projectprm392.database.AppDatabase;
import com.example.projectprm392.database.ReportDao;
import com.example.projectprm392.database.ReportEntity;

public class ReportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        EditText etTargetUserId = findViewById(R.id.etTargetUserId);
        EditText etJobId = findViewById(R.id.etJobId);
        EditText etContent = findViewById(R.id.etContent);
        Button btnSendReport = findViewById(R.id.btnSendReport);

        btnSendReport.setOnClickListener(v -> {
            String reporterId = "demo-reporter-id"; // TODO: Lấy từ user hiện tại
            String targetUserId = etTargetUserId.getText().toString().trim();
            String jobId = etJobId.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (targetUserId.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            ReportEntity report = new ReportEntity(reporterId, targetUserId, jobId, content);
            AppDatabase db = AppDatabase.getDatabase(this);
            ReportDao dao = db.reportDao();
            dao.insert(report);
            AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Gửi báo cáo thành công")
                .setMessage("Báo cáo của bạn đã được gửi. Cảm ơn bạn đã giúp xây dựng cộng đồng an toàn hơn!")
                .setPositiveButton("Đóng", (d, which) -> {
                    d.dismiss();
                    etTargetUserId.setText("");
                    etJobId.setText("");
                    etContent.setText("");
                })
                .setCancelable(false)
                .create();
            dialog.show();
        });
    }
}
