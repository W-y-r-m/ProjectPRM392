package com.example.projectprm392.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.projectprm392.R;
import com.example.projectprm392.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class PostTypeSelectionActivity extends AppCompatActivity {

    private MaterialCardView cardJobSeeking;
    private MaterialCardView cardJobPosting;
    private MaterialButton btnJobSeeking;
    private MaterialButton btnJobPosting;
    private Toolbar toolbar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_post_type_selection);

            initViews();
            setupToolbar();
            setupListeners();
            checkUserAuth();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        cardJobSeeking = findViewById(R.id.cardJobSeeking);
        cardJobPosting = findViewById(R.id.cardJobPosting);
        btnJobSeeking = findViewById(R.id.btnJobSeeking);
        btnJobPosting = findViewById(R.id.btnJobPosting);
        
        sessionManager = new SessionManager(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chọn loại đăng tin");
        }
    }

    private void setupListeners() {
        // Job Seeking Card Click
        cardJobSeeking.setOnClickListener(v -> navigateToJobSeeking());
        btnJobSeeking.setOnClickListener(v -> navigateToJobSeeking());

        // Job Posting Card Click
        cardJobPosting.setOnClickListener(v -> navigateToJobPosting());
        btnJobPosting.setOnClickListener(v -> navigateToJobPosting());

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void checkUserAuth() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đăng tin", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void navigateToJobSeeking() {
        Intent intent = new Intent(this, CreateJobSeekingActivity.class);
        startActivity(intent);
    }

    private void navigateToJobPosting() {
        Intent intent = new Intent(this, CreateJobPostingActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
