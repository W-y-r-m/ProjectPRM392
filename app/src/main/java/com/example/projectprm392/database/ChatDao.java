package com.example.projectprm392.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatEntity chatMessage);

    @Update
    void update(ChatEntity chatMessage);

    @Delete
    void delete(ChatEntity chatMessage);

    // Lấy tất cả tin nhắn giữa 2 user cho 1 job
    @Query("SELECT * FROM chat_messages WHERE " +
           "((senderId = :userId1 AND receiverId = :userId2) OR " +
           "(senderId = :userId2 AND receiverId = :userId1)) AND " +
           "jobId = :jobId ORDER BY sentAt ASC")
    List<ChatEntity> getMessagesBetweenUsers(int userId1, int userId2, int jobId);

    // Lấy danh sách conversation (cuộc trò chuyện gần nhất với mỗi người)
    @Query("SELECT * FROM chat_messages WHERE " +
           "(senderId = :userId OR receiverId = :userId) " +
           "ORDER BY sentAt DESC")
    List<ChatEntity> getConversations(int userId);

    // Lấy conversation theo job (cuộc trò chuyện gần nhất với mỗi người cho job cụ thể)
    @Query("SELECT * FROM chat_messages WHERE " +
           "(senderId = :userId OR receiverId = :userId) AND " +
           "jobId = :jobId " +
           "ORDER BY sentAt DESC")
    List<ChatEntity> getConversationsByJob(int userId, int jobId);

    // Lấy tin nhắn chưa đọc
    @Query("SELECT * FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    List<ChatEntity> getUnreadMessages(int userId);

    // Đánh dấu tin nhắn đã đọc
    @Query("UPDATE chat_messages SET isRead = 1 WHERE " +
           "receiverId = :userId AND senderId = :senderId AND jobId = :jobId")
    void markMessagesAsRead(int userId, int senderId, int jobId);

    // Lấy tin nhắn mới nhất giữa 2 user
    @Query("SELECT * FROM chat_messages WHERE " +
           "((senderId = :userId1 AND receiverId = :userId2) OR " +
           "(senderId = :userId2 AND receiverId = :userId1)) AND " +
           "jobId = :jobId ORDER BY sentAt DESC LIMIT 1")
    ChatEntity getLatestMessage(int userId1, int userId2, int jobId);

    // Đếm tin nhắn chưa đọc
    @Query("SELECT COUNT(*) FROM chat_messages WHERE receiverId = :userId AND isRead = 0")
    int getUnreadMessageCount(int userId);
}
