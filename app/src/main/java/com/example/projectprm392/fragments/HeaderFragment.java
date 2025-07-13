package com.example.projectprm392.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectprm392.R;
import com.example.projectprm392.activities.ApplicationActivity;
import com.example.projectprm392.activities.MyApplicationActivity;
import com.example.projectprm392.activities.ChatActivity;
import com.example.projectprm392.activities.ConversationListActivity;
import com.example.projectprm392.activities.ProfileActivity;
import com.example.projectprm392.adapters.ChatDialogAdapter;
import com.example.projectprm392.controllers.LoginController;
import com.example.projectprm392.database.ChatEntity;
import com.example.projectprm392.database.DatabaseHelper;
import com.example.projectprm392.database.UserEntity;
import com.example.projectprm392.models.User;
import com.example.projectprm392.utils.SessionManager;
import com.example.projectprm392.views.LoginActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HeaderFragment extends Fragment {

    private ImageView ivLogo;
    private SearchView searchView;
    private ImageView ivChat;
    private ImageView ivNotification;
    private ImageView ivProfile;
    private MaterialButton btnLogin;
    private static final int PROFILE_REQUEST_CODE = 1001;

    private TextView tvUserName;
    private ImageView ivUserMenu;
    private LoginController loginController;
    private SessionManager sessionManager;

    public interface OnSearchListener {
        void onSearchQuery(String query);
    }

    private OnSearchListener onSearchListener;

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_header, container, false);

        initViews(view);
        setupController();
        setupListeners();
        updateUI();

        return view;
    }

    private void initViews(View view) {
        ivLogo = view.findViewById(R.id.ivLogo);
        searchView = view.findViewById(R.id.searchView);
        ivChat = view.findViewById(R.id.ivChat);
        ivNotification = view.findViewById(R.id.ivNotification);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvUserName = view.findViewById(R.id.tvUserName);
        ivUserMenu = view.findViewById(R.id.ivUserMenu);
    }

    private void setupController() {
        loginController = new LoginController(requireContext());
        sessionManager = new SessionManager(requireContext());
    }

    private void setupListeners() {
        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (onSearchListener != null) {
                    onSearchListener.onSearchQuery(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Chat click
        ivChat.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showChatDialog();
            } else {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để sử dụng chat", Toast.LENGTH_SHORT).show();
            }
        });

        // Notification click
        ivNotification.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Thông báo", Toast.LENGTH_SHORT).show();
        });

        // Profile click
        ivProfile.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                showUserMenu(v);
            } else {
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // Login button
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        // User menu
        if (ivUserMenu != null) {
            ivUserMenu.setOnClickListener(this::showUserMenu);
        }
    }

    private void showUserMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.user_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_application2) {
                // Navigate to application
                Intent intent = new Intent(requireContext(), MyApplicationActivity.class);
                startActivity(intent);
                return true;
            }
            if (id == R.id.menu_application) {
                // Navigate to application
                Intent intent = new Intent(requireContext(), ApplicationActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menu_profile) {
                // Navigate to profile
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivityForResult(intent, PROFILE_REQUEST_CODE);
                return true;
            } else if (id == R.id.menu_history) {
                // Navigate to history
                Toast.makeText(requireContext(), "Lịch sử", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menu_logout) {
                logout();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void logout() {
        loginController.logout();
        updateUI();
        // Navigate to login or refresh main activity
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void updateUI() {
        if (loginController.isLoggedIn()) {
            String fullName = sessionManager.getFullName();
            if (fullName != null && !fullName.isEmpty()) {
                btnLogin.setVisibility(View.GONE);
                tvUserName.setVisibility(View.VISIBLE);
                ivUserMenu.setVisibility(View.VISIBLE);
                tvUserName.setText("Xin chào, " + fullName);
            }
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            tvUserName.setVisibility(View.GONE);
            ivUserMenu.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Profile được update thành công, refresh UI
            updateUI();
        }
    }

    private void showChatDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chat_list);

        // Make dialog fullwidth
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Initialize views
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewChatDialog);
        LinearLayout emptyState = dialog.findViewById(R.id.llEmptyState);
        ImageView closeButton = dialog.findViewById(R.id.ivCloseDialog);
        MaterialButton viewAllButton = dialog.findViewById(R.id.btnViewAllChats);

        // Set up close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Set up view all button
        viewAllButton.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(requireContext(), ConversationListActivity.class);
            startActivity(intent);
        });

        // Load conversations
        loadConversationsForDialog(recyclerView, emptyState);

        dialog.show();
    }

    private void loadConversationsForDialog(RecyclerView recyclerView, LinearLayout emptyState) {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
            String userEmail = sessionManager.getEmail();
            UserEntity currentUser = databaseHelper.getUserByEmail(userEmail);

            if (currentUser != null) {
                // Get recent conversations
                List<ChatEntity> allConversations = databaseHelper.getConversations(currentUser.getId());

                if (allConversations.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyState.setVisibility(View.GONE);

                    // Set up RecyclerView
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    ChatDialogAdapter adapter = new ChatDialogAdapter(
                            requireContext(),
                            allConversations,
                            databaseHelper,
                            currentUser.getId(),
                            (otherUserId, jobId, otherUserName, jobTitle) -> {
                                // Open chat activity
                                Intent intent = new Intent(requireContext(), ChatActivity.class);
                                intent.putExtra("OTHER_USER_ID", otherUserId);
                                intent.putExtra("JOB_ID", jobId);
                                intent.putExtra("OTHER_USER_NAME", otherUserName);
                                intent.putExtra("JOB_TITLE", jobTitle);
                                startActivity(intent);
                            });
                    recyclerView.setAdapter(adapter);
                }
            }
        } catch (Exception e) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        }
    }
}
