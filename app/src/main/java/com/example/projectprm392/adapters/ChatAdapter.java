package com.example.projectprm392.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projectprm392.R;
import com.example.projectprm392.database.ChatEntity;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_MESSAGE_SENT = 1;
    private static final int TYPE_MESSAGE_RECEIVED = 2;

    private List<ChatEntity> messages;
    private int currentUserId;
    private DatabaseHelper databaseHelper;

    public ChatAdapter(List<ChatEntity> messages, int currentUserId, DatabaseHelper databaseHelper) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.databaseHelper = databaseHelper;
    }

    @Override
    public int getItemViewType(int position) {
        ChatEntity message = messages.get(position);
        if (message.getSenderId() == currentUserId) {
            return TYPE_MESSAGE_SENT;
        } else {
            return TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatEntity message = messages.get(position);
        
        if (holder instanceof SentMessageHolder) {
            ((SentMessageHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageHolder) {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<ChatEntity> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    public void addMessage(ChatEntity message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessageText);
            timeText = itemView.findViewById(R.id.tvMessageTime);
        }

        void bind(ChatEntity message) {
            messageText.setText(message.getContent());
            timeText.setText(formatTime(message.getSentAt()));
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, senderName;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessageText);
            timeText = itemView.findViewById(R.id.tvMessageTime);
            senderName = itemView.findViewById(R.id.tvSenderName);
        }

        void bind(ChatEntity message) {
            messageText.setText(message.getContent());
            timeText.setText(formatTime(message.getSentAt()));
            
            // Lấy tên người gửi
            try {
                UserEntity sender = databaseHelper.getUserById(message.getSenderId());
                if (sender != null) {
                    senderName.setText(sender.getFullName());
                } else {
                    senderName.setText("Unknown User");
                }
            } catch (Exception e) {
                senderName.setText("Unknown User");
            }
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
