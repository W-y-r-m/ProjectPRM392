package com.example.projectprm392.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.ChatAdapter;
import com.example.projectprm392.database.ChatEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText edtMessage;
    private ImageButton btnSend;
    private TextView tvChatTitle;
    private Toolbar toolbar;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private UserEntity currentUser;
    
    private int otherUserId;
    private int jobId;
    private String otherUserName;
    private String jobTitle;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadMessages();
        startAutoRefresh();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        toolbar = findViewById(R.id.toolbar);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        // Get current user from database using session
        String userEmail = sessionManager.getEmail();
        currentUser = databaseHelper.getUserByEmail(userEmail);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        otherUserId = intent.getIntExtra("OTHER_USER_ID", 0);
        jobId = intent.getIntExtra("JOB_ID", 0);
        otherUserName = intent.getStringExtra("OTHER_USER_NAME");
        jobTitle = intent.getStringExtra("JOB_TITLE");

        if (otherUserId == 0 || jobId == 0) {
            Toast.makeText(this, "Lỗi: Không thể tải thông tin chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        tvChatTitle.setText(otherUserName + " - " + jobTitle);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUser.getId(), databaseHelper);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadMessages() {
        try {
            List<ChatEntity> messages = databaseHelper.getMessagesBetweenUsers(
                    currentUser.getId(), otherUserId, jobId);
            chatAdapter.updateMessages(messages);
            
            // Đánh dấu tin nhắn đã đọc
            databaseHelper.markMessagesAsRead(currentUser.getId(), otherUserId, jobId);
            
            if (!messages.isEmpty()) {
                recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi tải tin nhắn", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendMessage() {
        String content = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        try {
            ChatEntity message = new ChatEntity();
            message.setSenderId(currentUser.getId());
            message.setReceiverId(otherUserId);
            message.setContent(content);
            message.setJobId(jobId);
            message.setSentAt(System.currentTimeMillis());

            databaseHelper.insertChatMessage(message);
            chatAdapter.addMessage(message);
            
            edtMessage.setText("");
            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi gửi tin nhắn", Toast.LENGTH_SHORT).show();
        }
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRefreshing) {
                    isRefreshing = true;
                    refreshMessages();
                    isRefreshing = false;
                }
                handler.postDelayed(this, 2000); // Refresh every 2 seconds
            }
        };
        handler.post(refreshRunnable);
    }

    private void refreshMessages() {
        try {
            List<ChatEntity> messages = databaseHelper.getMessagesBetweenUsers(
                    currentUser.getId(), otherUserId, jobId);
            
            if (messages.size() > chatAdapter.getItemCount()) {
                chatAdapter.updateMessages(messages);
                recyclerView.smoothScrollToPosition(messages.size() - 1);
                
                // Đánh dấu tin nhắn mới đã đọc
                databaseHelper.markMessagesAsRead(currentUser.getId(), otherUserId, jobId);
            }
        } catch (Exception e) {
            // Ignore errors during refresh
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
