package com.example.projectprm392.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectprm392.R;
import com.example.projectprm392.activities.ChatActivity;
import com.example.projectprm392.database.ChatEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.JobEntity;
import com.example.projectprm392.database.UserEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatDialogAdapter extends RecyclerView.Adapter<ChatDialogAdapter.ViewHolder> {
    
    private List<ChatEntity> conversations;
    private Map<Integer, UserEntity> userCache;
    private Map<Integer, JobEntity> jobCache;
    private DatabaseHelper databaseHelper;
    private Context context;
    private int currentUserId;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(int otherUserId, int jobId, String otherUserName, String jobTitle);
    }

    public ChatDialogAdapter(Context context, List<ChatEntity> conversations, 
                           DatabaseHelper databaseHelper, int currentUserId, 
                           OnChatClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.databaseHelper = databaseHelper;
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.userCache = new HashMap<>();
        this.jobCache = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatEntity conversation = conversations.get(position);
        
        // Get the other user (not current user)
        int otherUserId = conversation.getSenderId() == currentUserId ? 
                         conversation.getReceiverId() : conversation.getSenderId();
        
        UserEntity otherUser = getUserFromCache(otherUserId);
        JobEntity job = getJobFromCache(conversation.getJobId());
        
        if (otherUser != null) {
            holder.tvUserName.setText(otherUser.getFullName());
            // Set initial letter for avatar
            String initial = otherUser.getFullName().substring(0, 1).toUpperCase();
            holder.tvUserInitial.setText(initial);
        } else {
            holder.tvUserName.setText("Unknown User");
            holder.tvUserInitial.setText("U");
        }
        
        if (job != null) {
            holder.tvJobTitle.setText(job.getTitle());
        } else {
            holder.tvJobTitle.setText("Unknown Job");
        }
        
        holder.tvLastMessage.setText(conversation.getContent());
        holder.tvMessageTime.setText(formatTime(conversation.getSentAt()));
        
        // Show unread indicator
        if (!conversation.isRead() && conversation.getReceiverId() == currentUserId) {
            holder.tvUnreadIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnreadIndicator.setVisibility(View.GONE);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                String userName = otherUser != null ? otherUser.getFullName() : "Unknown";
                String jobTitle = job != null ? job.getTitle() : "Unknown Job";
                listener.onChatClick(otherUserId, conversation.getJobId(), userName, jobTitle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(conversations.size(), 5); // Limit to 5 items in dialog
    }
    
    private UserEntity getUserFromCache(int userId) {
        if (!userCache.containsKey(userId)) {
            try {
                UserEntity user = databaseHelper.getUserById(userId);
                userCache.put(userId, user);
            } catch (Exception e) {
                userCache.put(userId, null);
            }
        }
        return userCache.get(userId);
    }
    
    private JobEntity getJobFromCache(int jobId) {
        if (!jobCache.containsKey(jobId)) {
            try {
                JobEntity job = databaseHelper.getJobById(jobId);
                jobCache.put(jobId, job);
            } catch (Exception e) {
                jobCache.put(jobId, null);
            }
        }
        return jobCache.get(jobId);
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 24 * 60 * 60 * 1000) { // Less than 24 hours
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else if (diff < 7 * 24 * 60 * 60 * 1000) { // Less than 7 days
            SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } else { // More than 7 days
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
    
    public void updateConversations(List<ChatEntity> newConversations) {
        this.conversations = newConversations;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvJobTitle, tvLastMessage, tvMessageTime, tvUnreadIndicator, tvUserInitial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvJobTitle = itemView.findViewById(R.id.tvJobTitle);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvUnreadIndicator = itemView.findViewById(R.id.tvUnreadIndicator);
            tvUserInitial = itemView.findViewById(R.id.tvUserInitial);
        }
    }
}
