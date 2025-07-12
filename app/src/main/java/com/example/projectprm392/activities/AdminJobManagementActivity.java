```java
package com.example.jobmanagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jobmanagement.database.DatabaseHelper;
import com.example.jobmanagement.model.JobEntity;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class AdminJobManagementActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_job_management);

        databaseHelper = new DatabaseHelper(this);

        // ...existing code...
    }

    private void showEditJobDialog(JobEntity job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_job, null);

        // Initialize views
        TextInputEditText etJobTitle = dialogView.findViewById(R.id.etJobTitle);
        TextInputEditText etJobDescription = dialogView.findViewById(R.id.etJobDescription);
        TextInputEditText etJobSalary = dialogView.findViewById(R.id.etJobSalary);
        TextInputEditText etJobLocation = dialogView.findViewById(R.id.etJobLocation);
        AutoCompleteTextView spJobType = dialogView.findViewById(R.id.spJobType);
        TextInputEditText etJobExperience = dialogView.findViewById(R.id.etJobExperience);
        TextInputEditText etJobRequirements = dialogView.findViewById(R.id.etJobRequirements);
        TextInputEditText etJobBenefits = dialogView.findViewById(R.id.etJobBenefits);

        // Setup job type dropdown
        String[] jobTypes = {"JOB_POSTING", "JOB_SEEKING"};
        String[] jobTypeDisplays = {"Tuyển dụng", "Tìm việc"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, jobTypeDisplays);
        spJobType.setAdapter(adapter);

        // Fill current data
        etJobTitle.setText(job.getTitle());
        etJobDescription.setText(job.getDescription());

        // Parse salary to remove currency formatting
        String salaryText = job.getSalary();
        if (salaryText != null && !salaryText.equals("Thỏa thuận")) {
            try {
                // Remove currency symbols and formatting
                String numericSalary = salaryText.replaceAll("[^\\d]", "");
                etJobSalary.setText(numericSalary);
            } catch (Exception e) {
                etJobSalary.setText("");
            }
        }

        etJobLocation.setText(job.getLocation());

        // Set job type
        if ("JOB_POSTING".equals(job.getPostType())) {
            spJobType.setText("Tuyển dụng", false);
        } else if ("JOB_SEEKING".equals(job.getPostType())) {
            spJobType.setText("Tìm việc", false);
        }

        etJobExperience.setText(job.getExperience() != null ? job.getExperience() : "");
        etJobRequirements.setText(job.getRequirements() != null ? job.getRequirements() : "");
        etJobBenefits.setText(job.getBenefits() != null ? job.getBenefits() : "");

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Cancel button
        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        // Save button
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            // Validate inputs
            String title = etJobTitle.getText().toString().trim();
            String description = etJobDescription.getText().toString().trim();
            String location = etJobLocation.getText().toString().trim();

            if (title.isEmpty()) {
                etJobTitle.setError("Vui lòng nhập tiêu đề");
                return;
            }

            if (description.isEmpty()) {
                etJobDescription.setError("Vui lòng nhập mô tả");
                return;
            }

            if (location.isEmpty()) {
                etJobLocation.setError("Vui lòng nhập địa điểm");
                return;
            }

            // Get selected job type
            String selectedType = spJobType.getText().toString();
            String postType = "JOB_POSTING";
            if ("Tìm việc".equals(selectedType)) {
                postType = "JOB_SEEKING";
            }

            // Format salary
            String salaryInput = etJobSalary.getText().toString().trim();
            String formattedSalary = "Thỏa thuận";
            if (!salaryInput.isEmpty()) {
                try {
                    long salaryValue = Long.parseLong(salaryInput);
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                    formattedSalary = formatter.format(salaryValue);
                } catch (NumberFormatException e) {
                    formattedSalary = salaryInput;
                }
            }

            // Update job entity
            job.setTitle(title);
            job.setDescription(description);
            job.setSalary(formattedSalary);
            job.setLocation(location);
            job.setPostType(postType);
            job.setExperience(etJobExperience.getText().toString().trim());
            job.setRequirements(etJobRequirements.getText().toString().trim());
            job.setBenefits(etJobBenefits.getText().toString().trim());

            // Update in database
            new Thread(() -> {
                try {
                    boolean success = databaseHelper.updateJob(job);
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "Cập nhật bài đăng thành công", Toast.LENGTH_SHORT).show();
                            loadJobs();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Cập nhật bài đăng thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Log.e("AdminJobManagement", "Error updating job", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Lỗi cập nhật bài đăng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        dialog.show();
    }

    private void loadJobs() {
        // Implementation for loading jobs
    }
}
```