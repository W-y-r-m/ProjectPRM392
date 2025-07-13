package com.example.projectprm392.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.adapters.ConversationListAdapter;
import com.example.projectprm392.database.ChatEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConversationListActivity extends AppCompatActivity {
    
    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;
    private TextView tvEmptyState;
    private Toolbar toolbar;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private UserEntity currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadConversations();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewConversations);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        toolbar = findViewById(R.id.toolbar);
        
        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        
        // Get current user from database using session
        String userEmail = sessionManager.getEmail();
        currentUser = databaseHelper.getUserByEmail(userEmail);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tin nhắn");
        }
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConversationListAdapter(this, new ArrayList<>(), databaseHelper, currentUser.getId());
        recyclerView.setAdapter(adapter);
    }
    
    private void loadConversations() {
        try {
            // Get all messages for current user
            List<ChatEntity> allMessages = databaseHelper.getUnreadMessages(currentUser.getId());
            
            // Get all messages where user is sender or receiver
            List<ChatEntity> userMessages = new ArrayList<>();
            
            // This is a simplified approach - in real implementation, you'd want to optimize this
            // by creating a proper query in the DAO
            
            // For now, we'll get conversations by job
            // You might want to implement a proper getConversationsForUser method
            
            // Get unique conversations (latest message per user per job)
            Map<String, ChatEntity> latestMessages = new HashMap<>();
            
            // This is a placeholder - you should implement proper conversation grouping
            // For now, just show empty state
            
            if (userMessages.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                adapter.updateConversations(userMessages);
            }
            
        } catch (Exception e) {
            showEmptyState();
        }
    }
    
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }
    
    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
